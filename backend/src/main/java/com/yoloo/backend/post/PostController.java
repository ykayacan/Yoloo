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
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.endpointsvalidator.Guard;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.game.GameService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
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
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;
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
  public Post getPost(String postId, User user) throws NotFoundException {
    Post post = ofy().load().group(Post.ShardGroup.class).key(Key.<Post>create(postId)).now();

    Guard.checkNotFound(post, "Could not find post with ID: " + postId);

    return postShardService
        .mergeShards(post)
        .flatMap(__ -> voteService.checkPostVote(__, Key.create(user.getUserId())))
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
  public Post insertQuestion(String content, String tags, String groupId, Optional<String> mediaIds,
      Optional<Integer> bounty, User user) {

    return insertPost(content, tags, groupId, Optional.absent(), mediaIds, bounty,
        Post.PostType.QUESTION, user);
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
  public Post insertBlog(String title, String content, String tags, String groupId,
      Optional<String> mediaIds, Optional<Integer> bounty, User user) {

    return insertPost(content, tags, groupId, Optional.of(title), mediaIds, bounty,
        Post.PostType.BLOG, user);
  }

  private Post insertPost(String content, String tags, String groupId, Optional<String> title,
      Optional<String> mediaIds, Optional<Integer> bounty, Post.PostType postType, User user) {

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
          .map(Key::<Media>create)
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

    List<Media> medias = Collections.emptyList();
    if (mediaIds.isPresent()) {
      //noinspection SuspiciousMethodCalls
      medias = Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<Media>create)
          .map(fetched::get)
          .cast(Media.class)
          .toList();
    }

    // Create a new question from given inputs.
    Post post =
        postService.createPost(account, content, tags, group, title, bounty, medias, tracker,
            postType);

    // Increase post count.
    accountShardService.updateCounter(accountShard, AccountShardService.Update.POST_UP);

    List<Tag> tagList = tagService.updateTags(post.getTags());

    // Increase post count.
    group = group.withPostCount(group.getPostCount() + 1);

    // Start gamification check.
    List<Notifiable> notifiables = new ArrayList<>(5);
    gameService.addFirstQuestionBonus(record, tracker, notifiables::addAll);
    gameService.addAskQuestionPerDayBonus(record, tracker, notifiables::addAll);

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
  public Post updatePost(String postId, Optional<String> title, Optional<String> content,
      Optional<Integer> bounty, Optional<String> tags, Optional<String> mediaIds)
      throws NotFoundException {

    ImmutableList.Builder<Key<?>> keyBuilder = ImmutableList.builder();

    // Create post key from websafe post id.
    final Key<Post> postKey = Key.create(postId);
    keyBuilder.add(postKey);

    if (mediaIds.isPresent()) {
      Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<Media>create)
          .foreach(keyBuilder::add);
    }

    ImmutableList<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Post original = (Post) fetched.get(postKey);

    List<Media> medias = Collections.emptyList();
    if (mediaIds.isPresent()) {
      List<Key<Media>> originalMediaKeys =
          Ix.from(original.getMedias()).map(Media::getKey).toList();
      mediaService.deleteMedias(originalMediaKeys);
      ofy().delete().keys(originalMediaKeys);

      //noinspection SuspiciousMethodCalls
      Ix
          .from(StringUtil.splitToIterable(mediaIds.get(), ","))
          .map(Key::<Media>create)
          .map(fetched::get)
          .cast(Media.class)
          .foreach(medias::add);
    }

    return Single
        .just(original)
        .map(post -> title.isPresent() ? post.withTitle(title.get()) : post)
        .map(post -> bounty.isPresent() ? post.withBounty(bounty.get()) : post)
        .map(post -> content.isPresent() ? post.withContent(content.get()) : post)
        .map(post -> tags.isPresent() ? post.withTags(
            ImmutableSet.copyOf(splitToIterable(tags.get(), ","))) : post)
        .map(post -> post.withMedias(medias))
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
    final Key<Post> postKey = Key.create(postId);
    final Key<AccountShard> accountShardKey =
        accountShardService.getRandomShardKey(postKey.getParent());

    Map<Key<Object>, Object> map = ofy().load().keys(postKey, accountShardKey);
    @SuppressWarnings("SuspiciousMethodCalls") AccountShard shard =
        (AccountShard) map.get(accountShardKey);
    shard.decreasePostCount();

    @SuppressWarnings("SuspiciousMethodCalls") Post post = (Post) map.get(postKey);
    List<Media> medias = post.getMedias();

    List<Key<Media>> mediaKeys = Collections.emptyList();
    if (medias != null) {
      Ix.from(medias).map(Key::<Media>create).foreach(mediaKey -> {
        mediaKeys.add(mediaKey);
        deleteList.add(mediaKey);
      });
    }

    List<Key<Comment>> commentKeys = postService.getCommentKeys(postKey);

    List<Key<Feed>> feedKeys =
        ofy().load().type(Feed.class).filter(Feed.FIELD_POST + "=", postKey).keys().list();

    deleteList
        .addAll(commentShardService.createShardMapWithKey(commentKeys).keySet())
        .addAll(commentService.getVoteKeys(commentKeys))
        .addAll(commentKeys)
        .addAll(postService.getVoteKeys(postKey))
        .addAll(postShardService.createShardMapWithKey(postKey).keySet())
        .add(postKey)
        .addAll(feedKeys);

    ofy().transact(() -> {
      ofy().defer().save().entity(shard);
      ofy().defer().delete().keys(deleteList.build());

      if (medias != null) {
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
  public CollectionResponse<Post> listPosts(Optional<String> userId, Optional<PostSorter> sorter,
      Optional<String> groupId, Optional<String> tags, Optional<Integer> limit,
      Optional<String> cursor, Optional<Post.PostType> postType, User user) {

    Query<Post> query = ofy().load().group(Post.ShardGroup.class).type(Post.class);

    if (postType.isPresent()) {
      query = query.filter(Post.FIELD_POST_TYPE + " =", postType.get());
    }

    query = userId.isPresent() ? query.ancestor(Key.<Account>create(userId.get())) : query;

    if (groupId.isPresent()) {
      query =
          query.filter(Post.FIELD_GROUP_KEY + " =", Key.<TravelerGroupEntity>create(groupId.get()));
    }

    if (tags.isPresent()) {
      List<String> tagSet = split(tags.get(), ",");

      for (String tagName : tagSet) {
        query = query.filter(Post.FIELD_TAGS + " =", tagName.trim().toLowerCase());
      }
    }

    query = PostSorter.sort(query, sorter.or(PostSorter.NEWEST));
    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    List<Post> posts = new ArrayList<>(DEFAULT_LIST_LIMIT);

    QueryResultIterator<Post> qi = query.iterator();

    while (qi.hasNext()) {
      posts.add(qi.next());
    }

    return Observable
        .just(posts)
        .flatMap(postShardService::mergeShards)
        .flatMap(__ -> voteService.checkPostVote(__, Key.create(user.getUserId())))
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
    Post post = ofy().load().key(Key.<Post>create(postId)).now();
    post.toBuilder().reportedByKey(Key.create(user.getUserId())).build();

    ofy().save().entity(post);
  }
}