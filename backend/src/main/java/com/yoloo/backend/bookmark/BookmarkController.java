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
import com.yoloo.backend.post.PostShard;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.vote.VoteService;
import io.reactivex.Observable;
import ix.Ix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.ofy;

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
  public void insertBookmark(String postId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());

    final Key<PostEntity> postKey = Key.create(postId);

    Bookmark saved = createBookmark(postKey, authKey);

    final Key<PostShard> postShardKey = postShardService.getRandomShardKey(postKey);

    PostShard shard = ofy().load().key(postShardKey).now();
    shard.getBookmarkKeys().add(saved.getKey());

    ofy().save().entities(saved, shard);
  }

  /**
   * Delete saved question.
   *
   * @param postId the websafe question id
   * @param user the user
   */
  public void deleteBookmark(String postId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());
    final Key<PostEntity> postKey = Key.create(postId);

    final Key<Bookmark> bookmarkKey = Key.create(authKey, Bookmark.class, postId);

    PostShard updatedShard = Ix
        .range(1, ShardConfig.POST_SHARD_COUNTER)
        .map(integer -> PostShard.createKey(postKey, integer))
        .collectToList()
        .flatMap(keys -> Ix.from(ofy().load().keys(keys).values()))
        .filter(postShard -> postShard.getBookmarkKeys().contains(bookmarkKey))
        .doOnNext(postShard -> postShard.getBookmarkKeys().remove(bookmarkKey))
        .single();

    ofy().defer().save().entity(updatedShard);
    ofy().defer().delete().key(bookmarkKey);
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

    Collection<PostEntity> postEntities = ofy().load().keys(postKeys).values();

    return Observable
        .fromIterable(postEntities)
        .map(postEntity -> postEntity.withBookmarked(true))
        .toList()
        .flatMapObservable(posts -> postShardService.mergeShards(posts))
        .flatMap(posts -> voteService.checkPostVote(posts, authKey))
        .map(posts -> CollectionResponse.<PostEntity>builder()
            .setItems(posts)
            .setNextPageToken(qi.getCursor().toWebSafeString())
            .build())
        .blockingSingle();
  }

  private Bookmark createBookmark(Key<PostEntity> questionKey, Key<Account> parentKey) {
    return Bookmark
        .builder()
        .id(questionKey.toWebSafeString())
        .parent(parentKey)
        .created(DateTime.now())
        .build();
  }
}