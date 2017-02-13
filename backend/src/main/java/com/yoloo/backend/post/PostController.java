package com.yoloo.backend.post;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
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
import com.yoloo.backend.game.GamificationService;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.media.Media;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.NotificationBundle;
import com.yoloo.backend.post.sort_strategy.PostSorter;
import com.yoloo.backend.tag.TagShard;
import com.yoloo.backend.tag.TagShardService;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.vote.VoteService;
import io.reactivex.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.util.StringUtil.split;
import static com.yoloo.backend.util.StringUtil.splitToSet;

@AllArgsConstructor(staticName = "create")
public final class PostController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(PostController.class.getName());

  /**
   * Maximum number of questions to return.
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
    final Key<Account> accountKey = Key.create(user.getUserId());

    // Create post key from websafe id.
    final Key<Post> postKey = Key.create(postId);

    // Fetch question.
    Post post = ofy().load().group(Post.ShardGroup.class).key(postKey).now();

    Guard.checkNotFound(post, "Could not find post with ID: " + postId);

    return postShardService
        .mergeShards(post)
        .flatMap(post1 -> voteService.checkPostVote(post1, accountKey, true))
        .blockingSingle();
  }

  /**
   * Add question.
   *
   * @param content the content
   * @param tags the tags
   * @param categories the categories
   * @param mediaId the media id
   * @param bounty the bounty
   * @param user the user
   * @return the question
   */
  public Post insertQuestion(
      String content,
      String tags,
      String categories,
      Optional<String> mediaId,
      Optional<Integer> bounty,
      User user) {

    return insertPost(content, tags, categories, Optional.absent(), mediaId, bounty,
        Post.PostType.QUESTION, user);
  }

  public Post insertBlog(
      String title,
      String content,
      String tags,
      String categories,
      Optional<String> mediaId,
      User user) {

    return insertPost(content, tags, categories, Optional.of(title), mediaId, Optional.absent(),
        Post.PostType.BLOG, user);
  }

  private Post insertPost(
      String content,
      String tags,
      String categories,
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
    Media media = (Media) fetched.get(mediaId.isPresent() ? Key.create(mediaId.get()) : null);

    // Create a new question from given inputs.
    PostEntity entity =
        postService.create(account, content, tags, categories, title, bounty, media, tracker,
            postType);

    Post post = entity.getPost();
    Collection<PostShard> shards = entity.getShards().values();

    // Increase total question count.
    accountShardService.updateCounter(accountShard, AccountShardService.Update.POST_UP);

    Collection<TagShard> tagShards = tagShardService.updateShards(post.getTags());

    Collection<CategoryShard> categoryShards =
        categoryShardService.updateShards(post.getCategories());

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

    FeedUpdateServlet.addToQueue(user.getUserId(), post.getWebsafeId());

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
        .map(post -> tags.isPresent() ? post.withTags(StringUtil.split(tags.get(), ",")) : post)
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

    Guard.checkNotFound(postKey, "Could not find post with ID: " + postId);

    Key<AccountShard> shardKey =
        accountShardService.getRandomShardKey(postKey.getParent());
    AccountShard shard = ofy().load().key(shardKey).now();
    shard.decreaseQuestions();

    // TODO: 27.11.2016 Change this operations to a push queue.

    List<Key<Comment>> commentKeys = postService.getCommentKeys(postKey);

    ofy().transact(() -> {
      ImmutableSet<Key<?>> deleteList = ImmutableSet.<Key<?>>builder()
          .addAll(commentShardService.createShardKeys(commentKeys))
          .addAll(commentService.getVoteKeys(commentKeys))
          .addAll(commentKeys)
          .addAll(postService.getVoteKeys(postKey))
          .addAll(postShardService.createShardMapWithKey(postKey).keySet())
          .add(postKey)
          .build();

      ofy().defer().save().entity(shard);
      ofy().defer().delete().keys(deleteList);
    });
  }

  /**
   * List collection response.
   *
   * @param accountId the account id
   * @param sorter the sorter
   * @param category the category
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
      Optional<String> category,
      Optional<String> tags,
      Optional<Integer> limit,
      Optional<String> cursor,
      Post.PostType postType,
      User user) {

    // Create account key from websafe id.
    final Key<Account> accountKey = Key.create(user.getUserId());

    // If sorter parameter is null, default sort strategy is "NEWEST".
    PostSorter postSorter = sorter.or(PostSorter.NEWEST);

    // Init query fetch request.
    Query<Post> query = ofy().load().type(Post.class)
        .filter(Post.FIELD_POST_TYPE + " =", postType);

    if (accountId.isPresent()) {
      query = query.ancestor(Key.<Account>create(accountId.get()));
    }

    if (category.isPresent()) {
      Set<String> categorySet = split(category.get(), ",");

      for (String categoryName : categorySet) {
        query = query.filter(Post.FIELD_CATEGORIES + " =", categoryName);
      }
    } else if (tags.isPresent()) {
      Set<String> tagSet = splitToSet(tags.get(), ",");

      for (String tagName : tagSet) {
        query = query.filter(Post.FIELD_TAGS + " =", tagName);
      }
    }

    // Sort by post sorter then edit query.
    query = PostSorter.sort(query, postSorter);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Post> qi = query.iterator();

    List<Post> posts = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      posts.add(qi.next());
    }

    posts = postShardService.mergeShards(posts)
        .flatMap(posts1 -> voteService.checkPostVote(posts1, accountKey, false))
        .blockingSingle();

    return CollectionResponse.<Post>builder()
        .setItems(posts)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  /**
   * Report.
   *
   * @param postId the websafe question id
   * @param user the user
   */
  public void reportPost(String postId, User user) throws NotFoundException {
    // Create question key from websafe question id.
    final Key<Post> questionKey = Key.create(postId);

    final Key<Account> reporterKey = Key.create(user.getUserId());

    Post post = ofy().load().key(questionKey).now();

    Guard.checkNotFound(post, "Could not find post with ID: " + postId);

    post.toBuilder().reportedByKey(reporterKey).build();

    ofy().save().entity(post);
  }
}