package com.yoloo.backend.bookmark;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.util.CollectionTransformer;
import com.yoloo.backend.vote.VoteService;
import io.reactivex.Observable;
import ix.Ix;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.endpointsvalidator.Guard.checkNotFound;

@AllArgsConstructor(staticName = "create")
public class BookmarkController extends Controller {

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private PostShardService postShardService;

  private VoteService voteService;

  /**
   * Save question.
   *
   * @param postId the websafe question id
   * @param user the user
   */
  public PostEntity insertBookmark(String postId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());

    final Key<PostEntity> postKey = Key.create(postId);

    Bookmark bookmark = createBookmark(postKey, authKey);

    final Key<PostEntity.PostShard> postShardKey = postShardService.getRandomShardKey(postKey);

    PostEntity.PostShard shard = ofy().load().key(postShardKey).now();
    shard.getBookmarkKeys().add(bookmark.getKey());

    ofy().save().entities(bookmark, shard);

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
   * Delete saved question.
   *
   * @param postId the websafe question id
   * @param user the user
   */
  public PostEntity deleteBookmark(String postId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());
    final Key<PostEntity> postKey = Key.create(postId);

    final Key<Bookmark> bookmarkKey = Key.create(authKey, Bookmark.class, postId);

    PostEntity.PostShard updatedShard = Ix
        .range(1, ShardConfig.POST_SHARD_COUNTER)
        .map(integer -> PostEntity.PostShard.createKey(postKey, integer))
        .collectToList()
        .flatMap(keys -> Ix.from(ofy().load().keys(keys).values()))
        .filter(postShard -> postShard.getBookmarkKeys().contains(bookmarkKey))
        .doOnNext(postShard -> postShard.getBookmarkKeys().remove(bookmarkKey))
        .single();

    ofy().defer().save().entity(updatedShard);
    ofy().defer().delete().key(bookmarkKey);

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
   * List collection response.
   *
   * @param limit the limit
   * @param cursor the cursor
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<PostEntity> listBookmarks(Optional<Integer> limit,
      Optional<String> cursor, User user) {
    // Create account key from websafe id.
    final Key<Account> authKey = Key.create(user.getUserId());

    // Init query fetch request.
    Query<Bookmark> query =
        ofy().load().type(Bookmark.class).ancestor(authKey).order("-" + Bookmark.FIELD_CREATED);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Bookmark> qi = query.iterator();

    List<Key<PostEntity>> postKeys = new ArrayList<>(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      postKeys.add(qi.next().getSavedPostKey());
    }

    return Observable
        .just(postKeys)
        .flatMap(keys -> Observable.fromCallable(
            () -> ofy().load().group(PostEntity.ShardGroup.class).keys(keys).values()))
        .flatMap(posts -> postShardService.mergeShards(posts, authKey))
        .flatMap(posts -> voteService.checkPostVote(posts, authKey))
        .compose(CollectionTransformer.create(qi.getCursor().toWebSafeString()))
        .blockingSingle();
  }

  private Bookmark createBookmark(Key<PostEntity> postKey, Key<Account> parentKey) {
    return Bookmark
        .builder()
        .id(postKey.toWebSafeString())
        .parent(parentKey)
        .created(DateTime.now())
        .build();
  }
}