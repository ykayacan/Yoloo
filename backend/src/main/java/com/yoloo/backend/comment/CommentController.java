package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.twitter.Extractor;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.device.DeviceUtil;
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
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.endpointsvalidator.Guard.checkNotFound;

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
  public Comment getComment(@Nonnull String commentId, @Nonnull User user)
      throws NotFoundException {
    return Observable
        .fromCallable(() -> {
          Comment comment = ofy().load()
              .group(Comment.ShardGroup.class)
              .key(Key.<Comment>create(commentId))
              .now();

          return checkNotFound(comment, "Comment does not exists!");
        })
        .flatMap(commentShardService::mergeShards)
        .flatMap(comment -> voteService.checkCommentVote(comment, Key.create(user.getUserId())))
        .blockingSingle();
  }

  /**
   * Insert comment comment.
   *
   * @param postId the post id
   * @param content the content
   * @param user the user
   * @return the comment
   */
  public Comment insertComment(@Nonnull String postId, @Nonnull String content,
      @Nonnull User user) {
    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create user key from user id.
    final Key<Account> commenterKey = Key.create(user.getUserId());
    keyBuilder.add(commenterKey);

    // Create post key from websafe id.
    final Key<PostEntity> postKey = Key.create(postId);
    keyBuilder.add(postKey);

    // Get a random shard key.
    final Key<PostShard> postShardKey = postShardService.getRandomShardKey(postKey);
    keyBuilder.add(postShardKey);

    // Create tracker key.
    final Key<Tracker> commenterTrackerKey = Tracker.createKey(commenterKey);
    keyBuilder.add(commenterTrackerKey);

    // Create record key.
    final Key<DeviceRecord> commenterRecordKey = DeviceRecord.createKey(commenterKey);
    keyBuilder.add(commenterRecordKey);

    // Create record key.
    final Key<DeviceRecord> postOwnerRecordKey = DeviceRecord.createKey(postKey.getParent());
    keyBuilder.add(postOwnerRecordKey);

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    // Make a batch load.
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(commenterKey);
    //noinspection SuspiciousMethodCalls
    PostEntity post = (PostEntity) fetched.get(postKey);
    //noinspection SuspiciousMethodCalls
    PostShard postShard = (PostShard) fetched.get(postShardKey);
    //noinspection SuspiciousMethodCalls
    Tracker commenterTracker = (Tracker) fetched.get(commenterTrackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord commenterRecord = (DeviceRecord) fetched.get(commenterRecordKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord postOwnerRecord = (DeviceRecord) fetched.get(postOwnerRecordKey);

    // Create a new comment from given inputs.
    CommentEntity entity = commentService.createComment(account, postKey, content);

    Comment comment = entity.getComment();
    Collection<CommentShard> commentShards = entity.getShards().values();

    // make post commented if already not
    post = post.withCommented(!post.isCommented());

    // Increase total comment count.
    postShard.increaseCommentCount();

    // Start gamification check.
    List<Notifiable> notifiables = new ArrayList<>();
    gameService.addAnswerFirstPostBonus(commenterRecord, commenterTracker, notifiables::addAll);
    gameService.addFirstCommenterBonus(commenterRecord, commenterTracker, post,
        notifiables::addAll);
    gameService.addAnswerToUnansweredQuestionBonus(commenterRecord, commenterTracker, post,
        notifiables::addAll);

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet
        .builder()
        .add(comment)
        .addAll(commentShards)
        .add(post)
        .add(postShard)
        .add(commenterTracker);

    for (Notifiable bundle : notifiables) {
      saveBuilder.addAll(bundle.getNotifications());
    }

    boolean sendCommentNotification = true;

    List<String> mentions = EXTRACTOR.extractMentionedScreennames(content);

    if (!mentions.isEmpty()) {
      sendCommentNotification = false;

      MentionNotifiable mentionNotifiable = getMentionNotifiable(account, post, comment, mentions);

      notifiables.add(mentionNotifiable);
      saveBuilder.addAll(mentionNotifiable.getNotifications());
    }

    // Do not send notification to self.
    if (!postKey.<Account>getParent().equivalent(commenterKey) && sendCommentNotification) {
      CommentNotifiable commentNotifiable =
          CommentNotifiable.create(account, postOwnerRecord, comment, post);

      notifiables.add(commentNotifiable);
      saveBuilder.addAll(commentNotifiable.getNotifications());
    }

    return ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (Notifiable bundle : notifiables) {
        notificationService.send(bundle);
      }

      return comment;
    });
  }

  private MentionNotifiable getMentionNotifiable(Account account, PostEntity post, Comment comment,
      List<String> mentions) {
    List<Key<Account>> accountKeys = findMentionedAccountKeys(mentions);

    Collection<DeviceRecord> records =
        ofy().load().keys(DeviceUtil.createKeysFromAccount(accountKeys)).values();

    return MentionNotifiable.create(post, account, records, comment);
  }

  private List<Key<Account>> findMentionedAccountKeys(List<String> mentions) {
    Query<Account> query = ofy().load().type(Account.class);

    for (final String username : mentions) {
      query.filter(Account.FIELD_USERNAME + " =", username);
    }

    return query.keys().list();
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
  public Comment updateComment(@Nonnull String postId, @Nonnull String commentId,
      Optional<String> content, Optional<Boolean> accepted, @Nonnull User user) {

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet.builder();

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create post key from websafe id.
    final Key<PostEntity> postKey = Key.create(postId);

    // Create comment key from websafe id.
    final Key<Comment> commentKey = Key.create(commentId);

    // Create device key from post owner.
    final Key<DeviceRecord> postOwnerRecordKey = DeviceRecord.createKey(postKey.getParent());

    // Create device key from comment owner.
    final Key<DeviceRecord> commenterRecordKey = DeviceRecord.createKey(commentKey.getParent());

    // Create tracker key from post owner.
    final Key<Tracker> postOwnerTrackerKey = Tracker.createKey(postKey.getParent());

    // Create tracker key from comment owner.
    final Key<Tracker> commenterTrackerKey = Tracker.createKey(commentKey.getParent());

    // Make a batch load.
    //noinspection unchecked
    Map<Key<Object>, Object> fetched = ofy()
        .load()
        .keys(accountKey, postKey, commentKey, postOwnerRecordKey, commenterRecordKey,
            postOwnerTrackerKey, commenterTrackerKey);

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    Comment comment = (Comment) fetched.get(commentKey);
    //noinspection SuspiciousMethodCalls
    PostEntity post = (PostEntity) fetched.get(postKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord postOwnerRecord = (DeviceRecord) fetched.get(postOwnerRecordKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord commenterRecord = (DeviceRecord) fetched.get(commenterRecordKey);
    //noinspection SuspiciousMethodCalls
    Tracker postOwnerTracker = (Tracker) fetched.get(postOwnerTrackerKey);
    //noinspection SuspiciousMethodCalls
    Tracker commenterTracker = (Tracker) fetched.get(commenterTrackerKey);

    comment = commentService.update(comment, content);

    // Start gamification check.
    List<Notifiable> notifiables = new ArrayList<>();
    if (accepted.isPresent()
        && post.getParent().equivalent(accountKey)
        && post.getAcceptedCommentKey() == null) {
      comment = comment.withAccepted(true);
      post = post.withAcceptedCommentKey(commentKey);
      post = gameService.addAcceptCommentBonus(postOwnerTracker, commenterTracker, postOwnerRecord,
          commenterRecord, post, notifiables::addAll, notifiables::addAll);

      notifiables.add(AcceptNotifiable.create(account, commenterRecord, post));
      saveBuilder.add(postOwnerRecord).add(commenterRecord);

      for (Notifiable bundle : notifiables) {
        saveBuilder.addAll(bundle.getNotifications());
      }
    }

    saveBuilder.add(post).add(comment);

    ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (Notifiable bundle : notifiables) {
        notificationService.send(bundle);
      }
    });

    comment = commentShardService
        .mergeShards(ofy().load().group(Comment.ShardGroup.class).key(commentKey).now())
        .flatMap(__ -> voteService.checkCommentVote(__, accountKey))
        .blockingSingle();

    return comment.withAccepted(post.getAcceptedCommentKey() != null);
  }

  /**
   * Remove.
   *
   * @param postId the websafe question id
   * @param commentId the websafe comment id
   * @param user the user
   */
  public void deleteComment(@Nonnull String postId, @Nonnull String commentId, @Nonnull User user) {
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
    shard.decreaseCommentCount();

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
  public CollectionResponse<Comment> listComments(@Nonnull String postId, Optional<String> cursor,
      Optional<Integer> limit, @Nonnull User user) {
    final Key<PostEntity> postKey = Key.create(postId);

    List<Comment> comments = new ArrayList<>(DEFAULT_LIST_LIMIT);

    Optional<Long> acceptedCommentId = Optional.absent();

    // Add accepted comment to response if no cursor position is given.
    if (!cursor.isPresent()) {
      PostEntity post = ofy().load().key(postKey).now();
      if (post.getAcceptedCommentKey() != null) {
        Comment acceptedComment = ofy().load()
            .group(Comment.ShardGroup.class)
            .key(post.getAcceptedCommentKey())
            .now();
        comments.add(acceptedComment);
        acceptedCommentId = Optional.of(acceptedComment.getId());
      }
    }

    Query<Comment> query = ofy()
        .load()
        .group(Comment.ShardGroup.class)
        .type(Comment.class)
        .filter(Comment.FIELD_POST_KEY + " =", postKey)
        .order("-" + Comment.FIELD_CREATED);

    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;
    query = query.limit(getCommentLimit(cursor, limit, acceptedCommentId.isPresent()));

    final QueryResultIterator<Comment> qi = query.iterator();

    while (qi.hasNext()) {
      Comment comment = qi.next();
      if (acceptedCommentId.isPresent()) {
        if (comment.getId() != acceptedCommentId.get()) {
          comments.add(comment);
        }
      } else {
        comments.add(comment);
      }
    }

    if (comments.isEmpty()) {
      return CollectionResponse.<Comment>builder().build();
    }

    return commentShardService
        .mergeShards(comments)
        .flatMap(__ -> voteService.checkCommentVote(__, Key.create(user.getUserId())))
        .map(__ -> CollectionResponse.<Comment>builder()
            .setItems(__)
            .setNextPageToken(qi.getCursor().toWebSafeString())
            .build())
        .blockingFirst();
  }

  private int getCommentLimit(Optional<String> cursor, Optional<Integer> limit,
      boolean hasAcceptedComment) {
    if (cursor.isPresent()) {
      return limit.or(DEFAULT_LIST_LIMIT);
    }

    return limit.isPresent()
        ? (hasAcceptedComment ? limit.get() - 1 : limit.get())
        : (hasAcceptedComment ? DEFAULT_LIST_LIMIT - 1 : DEFAULT_LIST_LIMIT);
  }
}