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
import com.yoloo.backend.endpointsvalidator.Guard;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.AcceptNotifiable;
import com.yoloo.backend.notification.type.CommentNotifiable;
import com.yoloo.backend.notification.type.MentionNotifiable;
import com.yoloo.backend.notification.type.Notifiable;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostShard;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class CommentController extends Controller {

  private static final Extractor EXTRACTOR = new Extractor();

  /**
   * Maximum number of comments to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private CommentService commentService;

  private CommentShardService commentShardService;

  private PostShardService postShardService;

  private GameService gameService;

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
    Comment comment = ofy().load().group(Comment.ShardGroup.class).key(commentKey).now();

    Guard.checkNotFound(comment, "Comment does not exists!");

    return commentShardService
        .mergeShards(comment)
        .flatMap(comment1 -> voteService.checkCommentVote(comment1, accountKey))
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
    final Key<PostEntity> postKey = Key.create(postId);
    keyBuilder.add(postKey);

    // Get a random shard key.
    final Key<PostShard> questionShardKey = postShardService.getRandomShardKey(postKey);
    keyBuilder.add(questionShardKey);

    // Create tracker key.
    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);
    keyBuilder.add(trackerKey);

    // Create record key.
    final Key<DeviceRecord> recordKey = DeviceRecord.createKey(accountKey);
    keyBuilder.add(recordKey);

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    // Make a batch load.
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    PostEntity postEntity = (PostEntity) fetched.get(postKey);
    //noinspection SuspiciousMethodCalls
    PostShard qqs = (PostShard) fetched.get(questionShardKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);

    // Create a new comment from given inputs.
    CommentEntity entity = commentService.createComment(account, postKey, content);

    Comment comment = entity.getComment();
    Collection<CommentShard> shards = entity.getShards().values();

    postEntity = postEntity.withCommented(!postEntity.isCommented());

    // Increase total comment count.
    qqs.increaseComments();

    // Start gamification check.
    List<Notifiable> notifiables = Lists.newArrayList();
    gameService.addAnswerFirstPostBonus(record, tracker, notifiables::addAll);
    gameService.addFirstCommenterBonus(record, tracker, postEntity, notifiables::addAll);
    gameService.addAnswerToUnansweredQuestionBonus(record, tracker, postEntity,
        notifiables::addAll);

    ImmutableSet.Builder<Object> saveBuilder =
        ImmutableSet.builder().add(comment).addAll(shards).add(qqs).add(postEntity).add(tracker);

    for (Notifiable bundle : notifiables) {
      saveBuilder.addAll(bundle.getNotifications());
    }

    // Do not send notification to self.
    if (!postKey.<Account>getParent().equivalent(accountKey)) {
      boolean sendCommentNotification = true;

      List<String> mentions = EXTRACTOR.extractMentionedScreennames(content);

      if (!mentions.isEmpty()) {
        Query<Account> query = ofy().load().type(Account.class);

        for (final String username : mentions) {
          query.filter(Account.FIELD_USERNAME + " =", username);
        }

        List<Key<Account>> accountKeys = query.keys().list();

        Collection<DeviceRecord> records =
            ofy().load().keys(DeviceUtil.createKeysFromAccount(accountKeys)).values();

        MentionNotifiable mentionNotifiable =
            MentionNotifiable.create(postEntity, account, records, comment);

        notificationService.send(mentionNotifiable);

        sendCommentNotification = false;
      }

      CommentNotifiable commentNotifiable =
          CommentNotifiable.create(account, record, comment, postEntity);

      if (sendCommentNotification) {
        notificationService.send(commentNotifiable);
      }

      saveBuilder.addAll(commentNotifiable.getNotifications());
    }

    return ofy().transact(() -> {
      Map<Key<Object>, Object> saved = ofy().save().entities(saveBuilder.build()).now();

      for (Notifiable bundle : notifiables) {
        notificationService.send(bundle);
      }

      //noinspection SuspiciousMethodCalls
      return (Comment) saved.get(comment.getKey());
    });
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
  public Comment updateComment(String postId, String commentId, Optional<String> content,
      Optional<Boolean> accepted, User user) {

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet.builder();

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create post key from websafe id.
    final Key<PostEntity> postKey = Key.create(postId);

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
    Map<Key<Object>, Object> fetched = ofy()
        .load()
        .keys(accountKey, postKey, commentKey, askerRecordKey, answererRecordKey, askerTrackerKey,
            answererTrackerKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Comment comment = (Comment) fetched.get(commentKey);
    //noinspection SuspiciousMethodCalls
    PostEntity postEntity = (PostEntity) fetched.get(postKey);
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
    List<Notifiable> notifiables = Lists.newArrayList();
    if (accepted.isPresent() && postEntity.getParent().equivalent(accountKey)) {

      postEntity = postEntity.withAcceptedCommentKey(comment.getKey());
      comment = comment.withAccepted(true);
      postEntity = gameService.addAcceptCommentBonus(askerTracker, answererTracker, askerRecord,
          answererRecord, postEntity, notifiables::addAll, notifiables::addAll);

      notifiables.add(AcceptNotifiable.create(account, answererRecord, postEntity));

      saveBuilder.add(askerRecord).add(answererRecord);

      for (Notifiable bundle : notifiables) {
        saveBuilder.addAll(bundle.getNotifications());
      }
    }

    saveBuilder.add(postEntity).add(comment);

    ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (Notifiable bundle : notifiables) {
        notificationService.send(bundle);
      }
    });

    return commentShardService
        .mergeShards(ofy().load().group(Comment.ShardGroup.class).key(commentKey).now())
        .flatMap(__ -> voteService.checkCommentVote(__, accountKey))
        .blockingSingle();
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
    final Key<PostEntity> postKey = Key.create(postId);
    final Key<PostShard> shardKey = postShardService.getRandomShardKey(postKey);

    final List<Key<Vote>> voteKeys = ofy()
        .load()
        .type(Vote.class)
        .filter(Vote.FIELD_VOTABLE_KEY + " =", commentKey)
        .keys()
        .list();

    PostShard shard = ofy().load().key(shardKey).now();
    shard.decreaseComments();

    // Immutable helper listFeed object to save all entities in a single db write.
    final ImmutableList<Key<?>> deleteList = ImmutableList.<Key<?>>builder()
        .add(commentKey)
        .addAll(commentShardService.createShardMapWithKey(commentKey).keySet())
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
   * @param postId the websafe question id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<Comment> listComments(String postId, Optional<String> cursor,
      Optional<Integer> limit, User user) {
    final Key<Account> authKey = Key.create(user.getUserId());
    final Key<PostEntity> postKey = Key.create(postId);

    Query<Comment> query = ofy()
        .load()
        .group(Comment.ShardGroup.class)
        .type(Comment.class)
        .filter(Comment.FIELD_POST_KEY + " =", postKey)
        .order("-" + Comment.FIELD_CREATED);

    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;

    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Comment> qi = query.iterator();

    List<Comment> comments = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      Comment comment = qi.next();
      if (!comment.isAccepted()) {
        comments.add(comment);
      }
    }

    if (!comments.isEmpty()) {
      comments = commentShardService
          .mergeShards(comments)
          .flatMap(comments1 -> voteService.checkCommentVote(comments1, authKey))
          .blockingFirst();
    }

    return CollectionResponse.<Comment>builder()
        .setItems(comments)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }
}