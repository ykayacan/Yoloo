package com.yoloo.backend.post;

import com.annimon.stream.Stream;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShard;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.category.CategoryShard;
import com.yoloo.backend.category.CategoryShardService;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.endpointsvalidator.Guard;
import com.yoloo.backend.game.GamificationService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.NotificationBundle;
import com.yoloo.backend.post.sort_strategy.PostSorter;
import com.yoloo.backend.tag.TagShard;
import com.yoloo.backend.tag.TagShardService;
import com.yoloo.backend.util.CollectionTransformer;
import com.yoloo.backend.vote.VoteService;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.util.StringUtil.split;
import static com.yoloo.backend.util.StringUtil.splitToSet;

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

  private TagShardService tagShardService;

  private CategoryShardService categoryShardService;

  private AccountShardService accountShardService;

  private GamificationService gameService;

  private MediaService mediaService;

  private NotificationService notificationService;

  private VoteService voteService;

  /**
   * Get question.
   *
   * @param postId the websafe question id
   * @return the question
   * @throws NotFoundException the not found exception
   */
  public Post getPost(String postId, User user) throws NotFoundException {
    Post post = ofy().load().group(Post.ShardGroup.class)
        .key(Key.<Post>create(postId)).now();

    Guard.checkNotFound(post, "Could not find post with ID: " + postId);

    return postShardService
        .mergeShards(post)
        .flatMap(__ -> voteService.checkPostVote(__, Key.create(user.getUserId())))
        .blockingSingle();
  }

  /**
   * Add question.
   *
   * @param content the content
   * @param tags the tags
   * @param categoryIds the categories
   * @param mediaId the media id
   * @param bounty the bounty
   * @param user the user
   * @return the question
   */
  public Post insertQuestion(
      String content,
      String tags,
      String categoryIds,
      Optional<String> mediaId,
      Optional<Integer> bounty,
      User user) {

    return insertPost(content, tags, categoryIds, Optional.absent(), mediaId, bounty,
        Post.PostType.QUESTION, user);
  }

  public Post insertBlog(
      String title,
      String content,
      String tags,
      String categoryIds,
      Optional<String> mediaId,
      User user) {

    return insertPost(content, tags, categoryIds, Optional.of(title), mediaId, Optional.absent(),
        Post.PostType.BLOG, user);
  }

  private Post insertPost(
      String content,
      String tags,
      String categoryIds,
      Optional<String> title,
      Optional<String> mediaId,
      Optional<Integer> bounty,
      Post.PostType postType,
      User user) {

    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

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

    if (mediaId.isPresent()) {
      final Key<Media> mediaKey = Key.create(mediaId.get());
      keyBuilder.add(mediaKey);
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
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
    Media media = (Media) fetched.get(mediaId.isPresent()
        ? Key.create(mediaId.get())
        : mediaId.orNull());

    // Create a new question from given inputs.
    PostEntity entity =
        postService.createPost(account, content, tags, categoryIds, title, bounty, media, tracker,
            postType);

    Post post = entity.getPost();
    Collection<PostShard> shards = entity.getShards().values();

    // Increase total question count.
    accountShardService.updateCounter(accountShard, AccountShardService.Update.POST_UP);

    Collection<TagShard> tagShards = tagShardService.updateShards(post.getTags());

    Collection<CategoryShard> categoryShards = categoryShardService.updateShards(categoryIds);

    // Start gamification check.
    List<NotificationBundle> notificationBundles = Lists.newArrayList();
    gameService.addFirstQuestionBonus(record, tracker, notificationBundles::addAll);
    gameService.addAskQuestionPerDayBonus(record, tracker, notificationBundles::addAll);

    ImmutableSet.Builder<Object> saveBuilder = ImmutableSet.builder()
        .add(post)
        .add(post)
        .addAll(shards)
        .addAll(tagShards)
        .addAll(categoryShards)
        .add(accountShard)
        .add(tracker);

    for (NotificationBundle bundle : notificationBundles) {
      saveBuilder.addAll(bundle.getNotifications());
    }

    ofy().transact(() -> {
      ofy().save().entities(saveBuilder.build()).now();

      for (NotificationBundle bundle : notificationBundles) {
        notificationService.send(bundle);
      }
    });

    UpdateFeedServlet.addToQueue(user.getUserId(), post.getWebsafeId());

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
   * @param mediaId the media id
   * @return the question
   * @throws NotFoundException the not found exception
   */
  public Post updatePost(
      String postId,
      Optional<String> title,
      Optional<String> content,
      Optional<Integer> bounty,
      Optional<String> tags,
      Optional<String> mediaId) throws NotFoundException {

    ImmutableSet.Builder<Key<?>> keyBuilder = ImmutableSet.builder();

    // Create post key from websafe post id.
    final Key<Post> postKey = Key.create(postId);
    keyBuilder.add(postKey);

    if (mediaId.isPresent()) {
      Key<Media> mediaKey = Key.create(mediaId.get());
      keyBuilder.add(mediaKey);
      // Deletes existing media item from cloud storage.
      mediaService.delete(mediaKey);
    }

    ImmutableSet<Key<?>> batchKeys = keyBuilder.build();
    Map<Key<Object>, Object> fetched =
        ofy().load().keys(batchKeys.toArray(new Key[batchKeys.size()]));

    //noinspection SuspiciousMethodCalls
    Media media = (Media) fetched
        .get(mediaId.isPresent() ? Key.<Media>create(mediaId.get()) : null);

    //noinspection SuspiciousMethodCalls
    return Single.just((Post) fetched.get(postKey))
        .map(post -> title.isPresent() ? post.withTitle(title.get()) : post)
        .map(post -> bounty.isPresent() ? post.withBounty(bounty.get()) : post)
        .map(post -> content.isPresent() ? post.withContent(content.get()) : post)
        .map(post -> tags.isPresent() ? post.withTags(split(tags.get(), ",")) : post)
        .map(post -> Optional.fromNullable(media).isPresent() ? post.withMedia(media) : post)
        .doOnSuccess(post -> ofy().transact(() -> ofy().save().entity(post).now()))
        .blockingGet();
  }

  /**
   * Remove question.
   *
   * @param postId the websafe question id
   * @throws NotFoundException the not found exception
   */
  public void deletePost(String postId) throws NotFoundException {
    // Create post key from websafe id.
    final Key<Post> postKey = Key.create(postId);

    AccountShard shard = ofy().load()
        .key(accountShardService.getRandomShardKey(postKey.getParent()))
        .now();
    shard.decreaseQuestions();

    postService.getCommentKeysObservable(postKey);

    List<Key<Comment>> commentKeys = postService.getCommentKeys(postKey);

    ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
        .addAll(commentShardService.createShardMapWithKey(commentKeys).keySet())
        .addAll(commentService.getVoteKeys(commentKeys))
        .addAll(commentKeys)
        .addAll(postService.getVoteKeys(postKey))
        .addAll(postShardService.createShardMapWithKey(postKey).keySet())
        .add(postKey)
        .build();

    ofy().transact(() -> {
      ofy().defer().save().entity(shard);
      ofy().defer().delete().keys(deleteList);
    });
  }

  /**
   * List collection response.
   *
   * @param accountId the account id
   * @param sorter the sorter
   * @param categories the category
   * @param tags the tags
   * @param limit the limit
   * @param cursor the cursor
   * @param postType the post type
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<Post> listPosts(
      Optional<String> accountId,
      Optional<PostSorter> sorter,
      Optional<String> categories,
      Optional<String> tags,
      Optional<Integer> limit,
      Optional<String> cursor,
      Post.PostType postType,
      User user) {

    return Observable
        .fromCallable(() -> {
          Query<Post> query = ofy().load()
              .group(Post.ShardGroup.class)
              .type(Post.class)
              .filter(Post.FIELD_POST_TYPE + " =", postType);

          query = accountId.isPresent()
              ? query.ancestor(Key.<Account>create(accountId.get()))
              : query;

          if (categories.isPresent()) {
            Set<String> categoryNames = split(categories.get(), ",");

            for (String categoryName : categoryNames) {
              query = query.filter(Post.FIELD_CATEGORIES + " =", categoryName);
            }
          }

          if (tags.isPresent()) {
            Set<String> tagSet = splitToSet(tags.get(), ",");

            for (String tagName : tagSet) {
              query = query.filter(Post.FIELD_TAGS + " =", tagName);
            }
          }

          query = PostSorter.sort(query, sorter.or(PostSorter.NEWEST));
          query = cursor.isPresent()
              ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
              : query;
          return query.limit(limit.or(DEFAULT_LIST_LIMIT));
        })
        .map(QueryResultIterable::iterator)
        .flatMap(qi -> Observable.fromIterable(Stream.of(qi).toList())
            .toList()
            .flatMapObservable(postShardService::mergeShards)
            .flatMap(__ -> voteService.checkPostVote(__, Key.create(user.getUserId())))
            .compose(CollectionTransformer.create(qi)))
        .blockingSingle();

    // Init query fetch request.
    /*Query<Post> query = ofy().load()
        .group(Post.ShardGroup.class)
        .type(Post.class)
        .filter(Post.FIELD_POST_TYPE + " =", postType);*/

    /*if (accountId.isPresent()) {
      query = query.ancestor(Key.<Account>create(accountId.get()));
    }

    if (categories.isPresent()) {
      Set<String> categoryNames = split(categories.get(), ",");

      for (String categoryName : categoryNames) {
        query = query.filter(Post.FIELD_CATEGORIES + " =", categoryName);
      }
    }

    if (tags.isPresent()) {
      Set<String> tagSet = splitToSet(tags.get(), ",");

      for (String tagName : tagSet) {
        query = query.filter(Post.FIELD_TAGS + " =", tagName);
      }
    }

    // Sort query.
    query = PostSorter.sort(query, sorter.or(PostSorter.NEWEST));

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Post> qi = query.iterator();

    //List<Post> postCount = Stream.of(qi).toList();

    return Observable.fromIterable(Stream.of(qi).toList())
        .toList()
        .flatMapObservable(postShardService::mergeShards)
        .flatMap(__ -> voteService.checkPostVote(__, Key.create(user.getUserId())))
        .compose(CollectionTransformer.create(qi))
        .blockingSingle();*/

    /*if (!postCount.isEmpty()) {
      postCount = postShardService.mergeShards(postCount)
          .flatMap(__ -> voteService.checkPostVote(__, accountKey))
          .blockingFirst();
    }

    return CollectionResponse.<Post>builder()
        .setItems(postCount)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();*/
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