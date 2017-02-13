package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
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
import com.yoloo.backend.game.GamificationService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.AcceptNotification;
import com.yoloo.backend.notification.type.CommentNotification;
import com.yoloo.backend.notification.type.MentionNotification;
import com.yoloo.backend.notification.type.NotificationBundle;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostShard;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class CommentController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(CommentController.class.getName());

  private static final Extractor EXTRACTOR = new Extractor();

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private CommentService commentService;

  private CommentShardService commentShardService;

  private PostShardService postShardService;

  private GamificationService gameService;

  private NotificationService notificationService;

  private VoteService voteService;

  /**
   * Get comment.
   *
   * @param commentId the websafe comment id
   * @param user the user
   * @return the comment
   */
  public Comment getComment(String commentId, User user) throws NotFoundException {

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
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey, true))
        .blockingSingle();
  }

  /**
   * Add comment.
   *
   * @param postId the websafe question id
   * @param content the content
   * @param user the user  @return the comment
   * @return the comment
   */
  public Comment insertComment(String postId, String content, User user) {

    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create user key from user id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    keyBuilder.add(accountKey);

    // Create post key from websafe id.
    final Key<Post> questionKey = Key.create(postId);
    keyBuilder.add(questionKey);

    // Get a random shard key.
    final Key<PostShard> questionShardKey = postShardService.getRandomShardKey(questionKey);
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
    Post post = (Post) fetched.get(questionKey);
    //noinspection SuspiciousMethodCalls
    PostShard qqs = (PostShard) fetched.get(questionShardKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);

    // Create a new comment from given inputs.
    CommentEntity model = commentService.create(account, questionKey, content);

    Comment comment = model.getComment();
    List<CommentShard> shards = model.getShards();

    post = post.withCommented(!post.isCommented());

    // Increase total comment count.
    qqs.increaseComments();

    // Start gamification check.
    List<NotificationBundle> notificationBundles = Lists.newArrayList();
    gameService.addFirstAnswerBonus(record, tracker, notificationBundles::addAll);
    gameService.addFirstAnswererPerDayBonus(record, tracker, post, notificationBundles::addAll);
    gameService.addAnswerToUnansweredQuestionBonus(record, tracker, post,
        notificationBundles::addAll);

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet.builder()
        .add(comment)
        .addAll(shards)
        .add(qqs)
        .add(post)
        .add(tracker);

    for (NotificationBundle bundle : notificationBundles) {
      saveBuilder.addAll(bundle.getNotifications());
    }

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

        Collection<DeviceRecord> records = ofy().load()
            .keys(DeviceUtil.createKeysFromAccount(accountKeys)).values();

        MentionNotification mentionNotification =
            MentionNotification.create(post, account, records, comment);

        notificationService.send(mentionNotification);

        sendCommentNotification = false;
      }

      CommentNotification commentNotification =
          CommentNotification.create(account, record, comment, post);

      if (sendCommentNotification) {
        notificationService.send(commentNotification);
      }

      saveBuilder.addAll(commentNotification.getNotifications());
    }

    ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (NotificationBundle bundle : notificationBundles) {
        notificationService.send(bundle);
      }
    });

    return comment;
  }

  /**
   * Update comment.
   *
   * @param postId the websafe question id
   * @param commentId the websafe comment id
   * @param content the content
   * @param accepted the accepted
   * @param user the user
   * @return the comment
   */
  public Comment updateComment(
      String postId,
      String commentId,
      Optional<String> content,
      Optional<Boolean> accepted,
      User user) {

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet.builder();

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create post key from websafe id.
    final Key<Post> postKey = Key.create(postId);

    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);

    // Create device key from post owner.
    final Key<DeviceRecord> askerRecordKey = DeviceRecord.createKey(postKey.getParent());

    // Create device key from comment owner.
    final Key<DeviceRecord> answererRecordKey = DeviceRecord.createKey(commentKey.getParent());

    // Create tracker key from post owner.
    final Key<Tracker> askerTrackerKey = Tracker.createKey(postKey.getParent());

    // Create tracker key from comment owner.
    final Key<Tracker> answererTrackerKey = Tracker.createKey(commentKey.getParent());

    // Make a batch load.
    //noinspection unchecked
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(accountKey, postKey, commentKey, askerRecordKey, answererRecordKey,
            askerTrackerKey, answererTrackerKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Comment comment = (Comment) fetched.get(commentKey);
    //noinspection SuspiciousMethodCalls
    Post post = (Post) fetched.get(postKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord askerRecord = (DeviceRecord) fetched.get(askerRecordKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord answererRecord = (DeviceRecord) fetched.get(answererRecordKey);
    //noinspection SuspiciousMethodCalls
    Tracker askerTracker = (Tracker) fetched.get(askerTrackerKey);
    //noinspection SuspiciousMethodCalls
    Tracker answererTracker = (Tracker) fetched.get(answererTrackerKey);

    comment = commentService.update(comment, content);

    // Start gamification check.
    List<NotificationBundle> notificationBundles = Lists.newArrayList();
    if (accepted.isPresent() && post.getParent().equivalent(accountKey)) {

      post = post.withAcceptedCommentKey(comment.getKey());
      comment = comment.withAccepted(true);
      post = gameService.addAcceptCommentBonus(askerTracker, answererTracker, askerRecord,
          answererRecord, post, notificationBundles::addAll, notificationBundles::addAll);

      notificationBundles.add(AcceptNotification.create(account, answererRecord, post));

      saveBuilder.add(askerRecord).add(answererRecord);

      for (NotificationBundle bundle : notificationBundles) {
        saveBuilder.addAll(bundle.getNotifications());
      }
    }

    saveBuilder.add(post).add(comment);

    ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (NotificationBundle bundle : notificationBundles) {
        notificationService.send(bundle);
      }
    });

    return comment;
  }

  /**
   * Remove.
   *
   * @param postId the websafe question id
   * @param commentId the websafe comment id
   * @param user the user
   */
  public void deleteComment(String postId, String commentId, User user) {
    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);
    final Key<Post> questionKey = Key.create(postId);
    final Key<PostShard> shardKey = postShardService.getRandomShardKey(questionKey);

    final List<Key<Vote>> voteKeys = ofy().load().type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", commentKey)
        .keys()
        .list();

    PostShard shard = ofy().load().key(shardKey).now();
    shard.decreaseComments();

    // Immutable helper listFeed object to save all entities in a single db write.
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
  public CollectionResponse<Comment> listComments(
      String questionId,
      Optional<String> cursor,
      Optional<Integer> limit,
      User user) {
    final Key<Account> authKey = Key.create(user.getUserId());
    final Key<Post> questionKey = Key.create(questionId);

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