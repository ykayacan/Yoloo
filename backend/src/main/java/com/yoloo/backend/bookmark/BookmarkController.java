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
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.question.QuestionUtil;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class BookmarkController extends Controller {

  private static final Logger logger =
      Logger.getLogger(BookmarkController.class.getName());

  /**
   * Maximum number of questions to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 20;

  @NonNull
  private BookmarkService bookmarkService;

  /**
   * Save question.
   *
   * @param questionId the websafe question id
   * @param user the user
   */
  public void add(String questionId, User user) {
    // Create user key from user id.
    final Key<Account> authKey = Key.create(user.getUserId());

    final Key<Question> questionKey = Key.create(questionId);

    Bookmark saved = bookmarkService.create(questionKey, authKey);

    ofy().save().entity(saved).now();
  }

  /**
   * Delete saved question.
   *
   * @param questionId the websafe question id
   * @param user the user
   */
  public void delete(String questionId, User user) {
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
  public CollectionResponse<Question> list(Optional<Integer> limit, Optional<String> cursor,
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

    List<Key<Question>> questionKeys = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      // Add fetched objects to map. Because cursor iteration needs to be iterated.
      questionKeys.add(qi.next().getSavedQuestionKey());
    }

    Collection<Question> questions = ofy().load().keys(questionKeys).values();

    questions = QuestionUtil.mergeCounts(questions)
        .toList(DEFAULT_LIST_LIMIT)
        .flatMap(questions1 -> QuestionUtil.mergeVoteDirection(questions1, authKey, true).toList())
        .blockingGet();

    return CollectionResponse.<Question>builder()
        .setItems(questions)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }
}