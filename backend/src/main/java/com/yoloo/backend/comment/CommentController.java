package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.gamification.reward.CommentTimeOutReward;
import com.yoloo.backend.gamification.reward.FirstCommentForQuestionReward;
import com.yoloo.backend.gamification.reward.FirstCommentReward;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.AcceptNotification;
import com.yoloo.backend.notification.type.CommentNotification;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.util.Group;
import com.yoloo.backend.vote.Vote;
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
  public Comment get(String commentId, User user) {
    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);

    // Fetch comment.
    Comment comment = ofy().load()
        .group(Comment.ShardGroup.class).key(commentKey).now();

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
   * @param mendtionIds the mendtion ids
   * @param user the user  @return the comment
   * @return the comment
   */
  public Comment add(String questionId, String content, Optional<String> mendtionIds, User user) {
    // Create user key from user id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create post key from websafe id.
    final Key<Question> questionKey = Key.create(questionId);

    // Get a random shard key.
    final Key<QuestionCounterShard> questionShardKey =
        questionShardService.getRandomShardKey(questionKey);

    // Create tracker key.
    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);

    // Create record key.
    final Key<DeviceRecord> recordKey =
        DeviceRecord.createKey(questionKey.getParent());

    /*List<Key<Account>> mentionedKeys = MentionHelper.mentionedAccountKeys(mendtionIds);
    List<Key<DeviceRecord>> mentionedRecordKeys = Lists.newArrayListWithCapacity(2);
    if (mentionedKeys != null) {
      for (Key<Account> key : mentionedKeys) {
        mentionedRecordKeys.add(DeviceRecord.createKey(key));
      }
    }*/

    ImmutableList<Key<?>> keys = ImmutableList.<Key<?>>builder()
        .add(accountKey)
        .add(questionKey)
        .add(questionShardKey)
        .add(trackerKey)
        .add(recordKey)
        .build();

    // Make a batch load.
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(keys.toArray(new Key[keys.size()]));

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

    CommentModel model = commentService.create(account, questionKey, content);

    // Increase total comment number.
    qqs.increaseComments();

    // Start gamification check.
    tracker = FirstCommentReward.of(tracker).getTracker();

    tracker = FirstCommentForQuestionReward.of(tracker, question).getTracker();

    tracker = CommentTimeOutReward.of(question, tracker).getTracker();

    if (!question.isCommented()) {
      question = question.withCommented(true);
    }

    CommentNotification commentNotification =
        CommentNotification.create(account, questionKey.getParent(), record, model.getComment());

    // Immutable helper list object to save all entities in a single db write.
    // For each single object use builder.add() method.
    // For each list object use builder.addAll() method.
    ImmutableList<Object> saveList = ImmutableList.builder()
        .add(model.getComment())
        .addAll(model.getShards())
        .add(qqs)
        .add(question)
        .add(tracker)
        .add(commentNotification.getNotification())
        .build();

    ofy().save().entities(saveList).now();

    notificationService.send(commentNotification);

    return model.getComment();
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
    ImmutableList.Builder<Object> saveBuilder = ImmutableList.builder();

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
        ofy().load().keys(questionKey, commentKey, recordKey, receiverTrackerKey);

    //noinspection SuspiciousMethodCalls
    Comment comment = (Comment) fetched.get(commentKey);
    //noinspection SuspiciousMethodCalls
    Question question = (Question) fetched.get(questionKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);
    //noinspection SuspiciousMethodCalls
    Tracker receiverTracker = (Tracker) fetched.get(receiverTrackerKey);

    comment = commentService.update(comment, content, accepted);
    Pair<Question, Comment> pair = commentService.accept(question, comment, accepted);

    ofy().transact(() -> {
      // If comment is accepted, then send notification to comment owner.
      if (accepted.isPresent()) {
        Group.OfTwo<Tracker, Question> group =
            gameService.exchangeBounties(receiverTracker, pair.first);

        saveBuilder.add(group.first);
        saveBuilder.add(group.second);
        saveBuilder.add(pair.second);

        AcceptNotification acceptNotification =
            AcceptNotification.create(commentKey.getParent(), record, question);
        saveBuilder.add(acceptNotification.getNotification());

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
      comments.add(qi.next());
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