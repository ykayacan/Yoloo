package com.yoloo.backend.post;

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
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.bookmark.Bookmark;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.MediaEntity;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.SendNewPostNotificationServlet;
import com.yoloo.backend.notification.type.Notifiable;
import com.yoloo.backend.post.sort_strategy.PostSorter;
import com.yoloo.backend.tag.Tag;
import com.yoloo.backend.tag.TagService;
import com.yoloo.backend.util.CollectionTransformer;
import com.yoloo.backend.util.ServerConfig;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.VoteService;
import io.reactivex.Observable;
import io.reactivex.Single;
import ix.Ix;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.endpointsvalidator.Guard.checkNotFound;
import static com.yoloo.backend.util.StringUtil.split;
import static com.yoloo.backend.util.StringUtil.splitToIterable;

@Log
@AllArgsConstructor(staticName = "create")
public final class PostController extends Controller {

  /**
   * Maximum number of postCount to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private PostService postService;

  private PostShardService postShardService;

  private CommentService commentService;

  private CommentShardService commentShardService;

  private AccountShardService accountShardService;

  private GameService gameService;

  private MediaService mediaService;

  private NotificationService notificationService;

  private VoteService voteService;

  private TagService tagService;

  /**
   * Get question.
   *
   * @param postId the websafe post id
   * @param user the user
   * @return the question
   * @throws NotFoundException the not found exception
   */
  public PostEntity getPost(String postId, User user) throws NotFoundException {
    return Observable
        .fromCallable(() -> {
          PostEntity post = ofy().load()
              .group(PostEntity.ShardGroup.class)
              .key(Key.<PostEntity>create(postId))
              .now();

          return checkNotFound(post, "Post does not exists!");
        })
        .flatMap(post -> postShardService.mergeShards(post, Key.create(user.getUserId())))
        .flatMap(comment -> voteService.checkPostVote(comment, Key.create(user.getUserId())))
        .blockingSingle();
  }

  /**
   * Insert question post.
   *
   * @param content the content
   * @param tags the tags
   * @param groupId the group id
   * @param mediaIds the media ids
   * @param bounty the bounty
   * @param user the user
   * @return the post
   */
  public PostEntity insertQuestionPost(String content, String tags, String groupId,
      Optional<String> mediaIds, Optional<Integer> bounty, User user) {
    final PostEntity.Type type =
        mediaIds.isPresent() ? PostEntity.Type.RICH_POST : PostEntity.Type.TEXT_POST;

    return insertPost(Optional.of(content), tags, groupId, Optional.absent(), mediaIds, bounty,
        type, user);
  }

  /**
   * Insert blog post.
   *
   * @param title the title
   * @param content the content
   * @param tags the tags
   * @param groupId the group id
   * @param mediaIds the media ids
   * @param bounty the bounty
   * @param user the user
   * @return the post
   */
  public PostEntity insertBlogPost(String title, String content, String tags, String groupId,
      Optional<String> mediaIds, Optional<Integer> bounty, User user) {

    return insertPost(Optional.of(content), tags, groupId, Optional.of(title), mediaIds, bounty,
        PostEntity.Type.BLOG, user);
  }

  /**
   * Insert image post post entity.
   *
   * @param tags the tags
   * @param groupId the group id
   * @param mediaIds the media ids
   * @param bounty the bounty
   * @param user the user
   * @return the post entity
   */
  public PostEntity insertImagePost(String tags, String groupId, Optional<String> mediaIds,
      Optional<Integer> bounty, User user) {

    return insertPost(Optional.absent(), tags, groupId, Optional.absent(), mediaIds, bounty,
        PostEntity.Type.IMAGE_POST, user);
  }

  private PostEntity insertPost(Optional<String> content, String tags, String groupId,
      Optional<String> title, Optional<String> mediaIds, Optional<Integer> bounty,
      PostEntity.Type type, User user) {

    ImmutableList.Builder<Key<?>> keyBuilder = ImmutableList.builder();

    // Create user key from user id.
    final Key<Account> accountKey = Key.create(user.getUserId());
    keyBuilder.add(accountKey);

    // Get a random shard key.
    final Key<AccountShard> accountShardKey = accountShardService.getRandomShardKey(accountKey);
    keyBuilder.add(accountShardKey);

    final Key<Tracker> trackerKey = Tracker.createKey(accountKey);
    keyBuilder.add(trackerKey);

    final Key<DeviceRecord> recordKey = DeviceRecord.createKey(accountKey);
    keyBuilder.add(recordKey);

    final Key<TravelerGroupEntity> groupKey = Key.create(groupId);
    keyBuilder.add(groupKey);

    if (mediaIds.isPresent()) {
      Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<MediaEntity>create)
          .foreach(keyBuilder::add);
    }

    ImmutableList<Key<?>> batchKeys = keyBuilder.build();
    // Make a batch load.
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Account account = (Account) fetched.get(accountKey);
    //noinspection SuspiciousMethodCalls
    AccountShard accountShard = (AccountShard) fetched.get(accountShardKey);
    //noinspection SuspiciousMethodCalls
    Tracker tracker = (Tracker) fetched.get(trackerKey);
    //noinspection SuspiciousMethodCalls
    DeviceRecord record = (DeviceRecord) fetched.get(recordKey);
    //noinspection SuspiciousMethodCalls
    TravelerGroupEntity group = (TravelerGroupEntity) fetched.get(groupKey);

    List<MediaEntity> medias = Collections.emptyList();
    if (mediaIds.isPresent()) {
      //noinspection SuspiciousMethodCalls
      medias = Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<MediaEntity>create)
          .map(fetched::get)
          .cast(MediaEntity.class)
          .toList();
    }

    // Create a new question from given inputs.
    PostEntity post =
        postService.createPost(account, content, tags, group, title, bounty, medias, tracker, type);

    // Increase post count.
    accountShardService.updateCounter(accountShard, AccountShardService.Update.POST_UP);

    List<Tag> tagList = tagService.updateTags(post.getTags());

    // Increase post count.
    group = group.withPostCount(group.getPostCount() + 1);

    // Start gamification check.
    List<Notifiable> notifiables = new ArrayList<>(5);
    /*RulesEngine engine = RulesEngineBuilder.aNewRulesEngine().build();

    engine.registerRule(new ShareFirstPostRule(tracker, record, notifiables));

    engine.fireRules();*/

    gameService.addShareFirstPostBonus(record, tracker, notifiables::addAll);
    gameService.addSharePostPerDayBonus(record, tracker, notifiables::addAll);

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet
        .builder()
        .add(post)
        .addAll(post.getShardMap().values())
        .addAll(tagList)
        .add(group)
        .add(accountShard)
        .add(tracker);

    for (Notifiable bundle : notifiables) {
      saveBuilder.addAll(bundle.getNotifications());
    }

    ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (Notifiable bundle : notifiables) {
        notificationService.send(bundle);
      }
    });

    if (!ServerConfig.isTest()) {
      UpdateFeedServlet.addToQueue(user.getUserId(), post.getWebsafeId());
      SendNewPostNotificationServlet.addToQueue(user.getUserId(), post.getWebsafeId());
    }

    return post;
  }

  /**
   * Update post.
   *
   * @param postId the question id
   * @param title the title
   * @param content the content
   * @param bounty the bounty
   * @param tags the tags
   * @param mediaIds the media ids
   * @return the question
   * @throws NotFoundException the not found exception
   */
  public PostEntity updatePost(String postId, Optional<String> title, Optional<String> content,
      Optional<Integer> bounty, Optional<String> tags, Optional<String> mediaIds)
      throws NotFoundException {

    ImmutableList.Builder<Key<?>> keyBuilder = ImmutableList.builder();

    // Create post key from websafe post id.
    final Key<PostEntity> postKey = Key.create(postId);
    keyBuilder.add(postKey);

    // Add media keys to builder if present
    if (mediaIds.isPresent()) {
      Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<MediaEntity>create)
          .foreach(keyBuilder::add);
    }

    // make batch request
    ImmutableList<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    PostEntity original = (PostEntity) fetched.get(postKey);

    List<MediaEntity> medias = Collections.emptyList();
    if (mediaIds.isPresent()) {
      List<Key<MediaEntity>> originalMediaKeys = Ix
          .from(original.getMedias())
          .map(postMedia -> Key.<MediaEntity>create(postMedia.getMediaId()))
          .toList();

      mediaService.deleteMedias(originalMediaKeys);
      ofy().delete().keys(originalMediaKeys);

      //noinspection SuspiciousMethodCalls
      Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<MediaEntity>create)
          .map(fetched::get)
          .cast(MediaEntity.class)
          .foreach(medias::add);
    }

    return Single
        .just(original)
        .map(post -> title.isPresent() ? post.withTitle(title.get()) : post)
        .map(post -> bounty.isPresent() ? post.withBounty(bounty.get()) : post)
        .map(post -> content.isPresent() ? post.withContent(content.get()) : post)
        .map(post -> tags.isPresent() ? post.withTags(
            ImmutableSet.copyOf(splitToIterable(tags.get(), ","))) : post)
        .map(post -> post.withMedias(Ix.from(medias).map(PostEntity.PostMedia::from).toList()))
        .doOnSuccess(post -> ofy().transact(() -> ofy().save().entity(post).now()))
        .blockingGet();
  }

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  public void deletePost(String postId) {
    ImmutableList.Builder<Key<?>> deleteList = ImmutableList.builder();

    // Create post key from websafe id.
    final Key<PostEntity> postKey = Key.create(postId);
    final Key<AccountShard> accountShardKey =
        accountShardService.getRandomShardKey(postKey.getParent());

    Map<Key<Object>, Object> map =
        ofy().load().group(PostEntity.ShardGroup.class).keys(postKey, accountShardKey);
    @SuppressWarnings("SuspiciousMethodCalls") AccountShard shard =
        (AccountShard) map.get(accountShardKey);
    shard.decreasePostCount();

    @SuppressWarnings("SuspiciousMethodCalls") PostEntity postEntity =
        (PostEntity) map.get(postKey);
    List<PostEntity.PostMedia> postMedias = postEntity.getMedias();

    List<Key<MediaEntity>> mediaKeys = Collections.emptyList();
    if (postMedias != null) {
      Ix
          .from(postMedias)
          .map(PostEntity.PostMedia::getMediaId)
          .map(Key::<MediaEntity>create)
          .foreach(mediaKey -> {
            mediaKeys.add(mediaKey);
            deleteList.add(mediaKey);
          });
    }

    List<Key<Comment>> commentKeys = postService.getCommentKeys(postKey);

    List<Key<Feed>> feedKeys =
        ofy().load().type(Feed.class).filter(Feed.FIELD_POST + "=", postKey).keys().list();

    Set<Key<Bookmark>> bookmarkKeys =
        Ix.from(postEntity.<PostShard>getShards()).reduce((s1, s2) -> {
          s2.getBookmarkKeys().addAll(s1.getBookmarkKeys());
          return s2;
        }).map(PostShard::getBookmarkKeys).single();

    deleteList
        .addAll(commentShardService.createShardMapWithKey(commentKeys).keySet())
        .addAll(commentService.getVoteKeys(commentKeys))
        .addAll(commentKeys)
        .addAll(postService.getVoteKeys(postKey))
        .addAll(postShardService.createShardMapWithKey(postKey).keySet())
        .addAll(bookmarkKeys)
        .add(postKey)
        .addAll(feedKeys);

    ofy().transact(() -> {
      ofy().defer().save().entity(shard);
      ofy().defer().delete().keys(deleteList.build());

      if (postMedias != null) {
        mediaService.deleteMedias(mediaKeys);
      }
    });
  }

  /**
   * List posts collection response.
   *
   * @param userId the user id
   * @param sorter the sorter
   * @param groupId the group id
   * @param tags the tags
   * @param limit the limit
   * @param cursor the cursor
   * @param postType the post type
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<PostEntity> listPosts(Optional<String> userId,
      Optional<PostSorter> sorter, Optional<String> groupId, Optional<String> tags,
      Optional<Integer> limit, Optional<String> cursor, Optional<PostEntity.Type> postType,
      User user) {

    Query<PostEntity> query =
        ofy().load().group(PostEntity.ShardGroup.class).type(PostEntity.class);

    if (postType.isPresent()) {
      query = query.filter(PostEntity.FIELD_POST_TYPE + " =", postType.get().getType());
    }

    query = userId.isPresent() ? query.ancestor(Key.<Account>create(userId.get())) : query;

    if (groupId.isPresent()) {
      query = query.filter(PostEntity.FIELD_GROUP_KEY + " =",
          Key.<TravelerGroupEntity>create(groupId.get()));
    }

    if (tags.isPresent()) {
      List<String> tagSet = split(tags.get(), ",");

      for (String tagName : tagSet) {
        query = query.filter(PostEntity.FIELD_TAGS + " =", tagName.trim().toLowerCase());
      }
    }

    query = PostSorter.sort(query, sorter.or(PostSorter.NEWEST));
    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    List<PostEntity> posts = new ArrayList<>(DEFAULT_LIST_LIMIT);

    QueryResultIterator<PostEntity> qi = query.iterator();

    while (qi.hasNext()) {
      posts.add(qi.next());
    }

    Key<Account> accountKey = Key.create(user.getUserId());

    if (posts.isEmpty()) {
      return CollectionResponse.<PostEntity>builder().build();
    }

    return Observable
        .just(posts)
        .flatMap(__ -> postShardService.mergeShards(__, accountKey))
        .flatMap(__ -> voteService.checkPostVote(__, accountKey))
        .compose(CollectionTransformer.create(qi.getCursor().toWebSafeString()))
        .blockingSingle();
  }

  public CollectionResponse<PostEntity> listMediaPosts(Optional<Integer> limit,
      Optional<String> cursor, User user) {

    Query<PostEntity> query = ofy()
        .load()
        .group(PostEntity.ShardGroup.class)
        .type(PostEntity.class)
        .filter(PostEntity.FIELD_HAS_MEDIA + " =", true);

    query = PostSorter.sort(query, PostSorter.NEWEST);
    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    List<PostEntity> posts = new ArrayList<>(DEFAULT_LIST_LIMIT);

    QueryResultIterator<PostEntity> qi = query.iterator();

    while (qi.hasNext()) {
      posts.add(qi.next());
    }

    Key<Account> accountKey = Key.create(user.getUserId());

    return Observable
        .just(posts)
        .flatMap(__ -> postShardService.mergeShards(__, accountKey))
        .flatMap(__ -> voteService.checkPostVote(__, accountKey))
        .compose(CollectionTransformer.create(qi.getCursor().toWebSafeString()))
        .blockingSingle();
  }

  /**
   * Report.
   *
   * @param postId the websafe question id
   * @param user the user
   */
  public void reportPost(String postId, User user) throws NotFoundException {
    PostEntity postEntity = ofy().load().key(Key.<PostEntity>create(postId)).now();
    postEntity.toBuilder().reportedByKey(Key.create(user.getUserId())).build();

    ofy().save().entity(postEntity);
  }
}