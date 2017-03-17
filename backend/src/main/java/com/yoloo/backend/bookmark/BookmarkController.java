package com.yoloo.backend.bookmark;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.post.dto.PostDTO;
import com.yoloo.backend.post.transformer.PostTransformer;
import com.yoloo.backend.vote.VoteService;
import ix.Ix;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class BookmarkController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(BookmarkController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  private PostTransformer postTransformer;

  private PostShardService postShardService;

  private VoteService voteService;

  /**
   * Save question.
   *
   * @param questionId the websafe question id
   * @param user the user
   */
  public void insertBookmark(String questionId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());

    final Key<Post> postKey = Key.create(questionId);

    Bookmark saved = createBookmark(postKey, authKey);

    ofy().save().entity(saved).now();
  }

  /**
   * Delete saved question.
   *
   * @param questionId the websafe question id
   * @param user the user
   */
  public void deleteBookmark(String questionId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());

    final Key<Bookmark> savedQuestionKey = Key.create(authKey, Bookmark.class, questionId);

    ofy().delete().key(savedQuestionKey);
  }

  /**
   * List collection response.
   *
   * @param limit the limit
   * @param cursor the cursor
   * @param user the user
   * @return the collection response
   */
  public CollectionResponse<PostDTO> listBookmarks(
      Optional<Integer> limit,
      Optional<String> cursor,
      User user) {
    // Create account key from websafe id.
    final Key<Account> authKey = Key.create(user.getUserId());

    // Init query fetch request.
    Query<Bookmark> query = ofy()
        .load()
        .type(Bookmark.class)
        .ancestor(authKey)
        .order("-" + Bookmark.FIELD_CREATED);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Bookmark> qi = query.iterator();

    List<Key<Post>> postKeys = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      postKeys.add(qi.next().getSavedPostKey());
    }

    Collection<Post> posts = ofy().load().keys(postKeys).values();

    List<PostDTO> postDTOs = postShardService.mergeShards(posts)
        .flatMap(__ -> voteService.checkPostVote(__, authKey))
        .map(__ -> Ix.from(__).map(post -> postTransformer.transformTo(post)).toList())
        .blockingSingle();

    return CollectionResponse.<PostDTO>builder()
        .setItems(postDTOs)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }

  private Bookmark createBookmark(Key<Post> questionKey, Key<Account> parentKey) {
    return Bookmark.builder()
        .id(questionKey.toWebSafeString())
        .parent(parentKey)
        .build();
  }
}