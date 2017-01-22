package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.twitter.Extractor;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.device.DeviceUtil;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.gamification.reward.CommentTimeOutReward;
import com.yoloo.backend.gamification.reward.FirstCommentForQuestionReward;
import com.yoloo.backend.gamification.reward.FirstCommentReward;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.AcceptNotification;
import com.yoloo.backend.notification.type.CommentNotification;
import com.yoloo.backend.notification.type.MentionNotification;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.util.Group;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.vote.Vote;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class CommentController extends Controller {

  private static final Logger logger =
      Logger.getLogger(CommentController.class.getName());

  private static final Extractor EXTRACTOR = new Extractor();

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  @NonNull
  private CommentService commentService;

  @NonNull
  private CommentShardService commentShardService;

  @NonNull
  private QuestionShardService questionShardService;

  @NonNull
  private GamificationService gameService;

  @NonNull
  private NotificationService notificationService;

  /**
   * Get comment.
   *
   * @param commentId the websafe comment id
   * @param user the user
   * @return the comment
   */
  public Comment get(String commentId, User user) throws NotFoundException {
    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);

    // Fetch comment.
    Comment comment = ofy().load()
        .group(Comment.ShardGroup.class).key(commentKey).now();

    Guard.checkNotFound(comment, "Comment does not exists!");

    return CommentUtil
        .mergeCommentCounts(comment)
        .flatMap(comment1 -> CommentUtil.mergeVoteDirection(comment1, accountKey))
        .blockingSingle();
  }

  /**
   * Add comment.
   *
   * @param questionId the websafe question id
   * @param content the content
   * @param user the user  @return the comment
   * @return the comment
   */
  public Comment add(String questionId, String content, User user) {
    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create user key from user id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    keyBuilder.add(accountKey);

    // Create post key from websafe id.
    final Key<Question> questionKey = Key.create(questionId);
    keyBuilder.add(questionKey);

    // Get a random shard key.
    final Key<QuestionCounterShard> questionShardKey =
        questionShardService.getRandomShardKey(questionKey);
    keyBuilder.add(questionShardKey);

    // Create tracker key.
    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);
    keyBuilder.add(trackerKey);

    // Create record key.
    final Key<DeviceRecord> recordKey = DeviceRecord.createKey(questionKey.getParent());
    keyBuilder.add(recordKey);

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    // Make a batch load.
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Question question = (Question) fetched.get(questionKey);
    //noinspection SuspiciousMethodCalls
    QuestionCounterShard qqs = (QuestionCounterShard) fetched.get(questionShardKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);

    // Create a new comment from given inputs.
    CommentModel model = commentService.create(account, questionKey, content);

    Comment comment = model.getComment();
    List<CommentCounterShard> shards = model.getShards();

    question = question.withCommented(!question.isCommented());

    // Increase total comment count.
    qqs.increaseComments();

    // Start gamification check.
    tracker = FirstCommentReward.of(tracker).getTracker();
    tracker = FirstCommentForQuestionReward.of(tracker, question).getTracker();
    tracker = CommentTimeOutReward.of(question, tracker).getTracker();

    // Immutable helper list object to save all entities in a single db write.
    // For each single object use builder.addAdmin() method.
    // For each list object use builder.addAll() method.
    ImmutableSet.Builder<Object> saveListBuilder = ImmutableSet.builder()
        .add(comment)
        .addAll(shards)
        .add(qqs)
        .add(question)
        .add(tracker);

    // Do not send notification to self.
    if (!questionKey.<Account>getParent().equivalent(accountKey)) {
      boolean sendCommentNotification = true;

      List<String> mentions = EXTRACTOR.extractMentionedScreennames(content);
      if (!mentions.isEmpty()) {
        Query<Account> query = ofy().load().type(Account.class);

        for (final String username : mentions) {
          query.filter(Account.FIELD_USERNAME + " =", username);
        }

        List<Key<Account>> accountKeys = query.keys().list();
        List<Key<DeviceRecord>> recordKeys = DeviceUtil.createKeysFromAccount(accountKeys);
        Collection<DeviceRecord> records = ofy().load().keys(recordKeys).values();

        MentionNotification mentionNotification =
            MentionNotification.create(question, account, records, comment);

        notificationService.send(mentionNotification);

        sendCommentNotification = false;
      }

      CommentNotification commentNotification =
          CommentNotification.create(account, record, comment, question);

      if (sendCommentNotification) {
        notificationService.send(commentNotification);
      }

      saveListBuilder.addAll(commentNotification.getNotifications());
    }

    ofy().transact(() -> ofy().save().entities(saveListBuilder.build()).now());

    return comment;
  }

  /**
   * Update comment.
   *
   * @param questionId the websafe question id
   * @param commentId the websafe comment id
   * @param content the content
   * @param accepted the accepted
   * @param user the user
   * @return the comment
   */
  public Comment update(String questionId, String commentId, Optional<String> content,
      Optional<Boolean> accepted, User user) {
    // Immutable helper list object to save all entities in a single db write.
    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet.builder();

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create question key from websafe id.
    final Key<Question> questionKey = Key.create(questionId);

    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);

    // Create device key from comment owner.
    final Key<DeviceRecord> recordKey = DeviceRecord.createKey(commentKey.getParent());

    // Create tracker key from comment owner.
    final Key<Tracker> receiverTrackerKey = Tracker.createKey(commentKey.getParent());

    // Make a batch load.
    //noinspection unchecked
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(accountKey, questionKey, commentKey, recordKey, receiverTrackerKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Comment comment = (Comment) fetched.get(commentKey);
    //noinspection SuspiciousMethodCalls
    Question question = (Question) fetched.get(questionKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);
    //noinspection SuspiciousMethodCalls
    Tracker receiverTracker = (Tracker) fetched.get(receiverTrackerKey);

    comment = commentService.update(comment, content);
    Pair<Question, Comment> pair = commentService.accept(question, comment, accepted, accountKey);

    ofy().transact(() -> {
      // If comment is accepted, then send notification to comment owner.
      if (accepted.isPresent()) {
        Group.OfTwo<Tracker, Question> group =
            gameService.exchangeBounties(receiverTracker, pair.first);

        saveBuilder.add(group.first);
        saveBuilder.add(group.second);
        saveBuilder.add(pair.second);

        AcceptNotification acceptNotification = AcceptNotification.create(account, record, question);
        saveBuilder.addAll(acceptNotification.getNotifications());

        notificationService.send(acceptNotification);
      } else {
        saveBuilder.add(pair.first);
        saveBuilder.add(pair.second);
      }

      ofy().save().entities(saveBuilder.build()).now();
    });

    return comment;
  }

  /**
   * Remove.
   *
   * @param questionId the websafe question id
   * @param commentId the websafe comment id
   * @param user the user
   */
  public void delete(String questionId, String commentId, User user) {
    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);
    final Key<Question> questionKey = Key.create(questionId);
    final Key<QuestionCounterShard> shardKey =
        questionShardService.getRandomShardKey(questionKey);

    final List<Key<Vote>> voteKeys = ofy().load().type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", commentKey)
        .keys()
        .list();

    QuestionCounterShard shard = ofy().load().key(shardKey).now();
    shard.decreaseComments();

    // Immutable helper list object to save all entities in a single db write.
    final ImmutableList<Key<?>> deleteList = ImmutableList.<Key<?>>builder()
        .add(commentKey)
        .addAll(commentShardService.createShardKeys(commentKey))
        .addAll(voteKeys)
        .build();

    ofy().transact(() -> {
      ofy().delete().keys(deleteList).now();
      ofy().save().entity(shard).now();
    });
  }

  /**
   * List collection response.
   *
   * @param questionId the websafe question id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<Comment> list(String questionId, Optional<String> cursor,
      Optional<Integer> limit, User user) {
    final Key<Account> authKey = Key.create(user.getUserId());
    final Key<Question> questionKey = Key.create(questionId);

    Query<Comment> query = ofy().load()
        .group(Comment.ShardGroup.class)
        .type(Comment.class)
        .filter(Comment.FIELD_QUESTION_KEY + " =", questionKey)
        .order("-" + Comment.FIELD_CREATED);

    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Comment> qi = query.iterator();

    List<Comment> comments = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      Comment comment = qi.next();
      if (!comment.isAccepted()) {
        comments.add(comment);
      }
    }

    comments = CommentUtil.mergeCommentCounts(comments)
        .toList(DEFAULT_LIST_LIMIT)
        .flatMap(comments1 -> CommentUtil.mergeVoteDirection(comments1, authKey).toList())
        .blockingGet();

    return CollectionResponse.<Comment>builder()
        .setItems(comments)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }
}