package com.yoloo.backend.saved;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.question.QuestionUtil;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;

import java.util.Map;
import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class SavedQuestionController extends Controller {

    private static final Logger logger =
            Logger.getLogger(SavedQuestionController.class.getName());

    /**
     * Maximum number of questions to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    @NonNull
    private SavedQuestionService savedQuestionService;

    @NonNull
    private QuestionShardService questionShardService;

    /**
     * Save question.
     *
     * @param websafeQuestionId the websafe question id
     * @param user              the user
     */
    public void add(String websafeQuestionId, User user) {
        // Create user key from user id.
        final Key<Account> authKey = Key.create(user.getUserId());

        final Key<Question> questionKey = Key.create(websafeQuestionId);

        SavedQuestion saved = savedQuestionService.create(questionKey, authKey);

        ofy().save().entity(saved);
    }

    /**
     * Delete saved question.
     *
     * @param websafeQuestionId the websafe question id
     * @param user              the user
     */
    public void delete(String websafeQuestionId, User user) {
        // Create user key from user id.
        final Key<Account> authKey = Key.create(user.getUserId());

        final Key<SavedQuestion> savedQuestionKey =
                Key.create(authKey, SavedQuestion.class, websafeQuestionId);

        ofy().delete().key(savedQuestionKey);
    }

    /**
     * List collection response.
     *
     * @param limit  the limit
     * @param cursor the cursor
     * @param user   the user
     * @return the collection response
     */
    public CollectionResponse<Question> list(Optional<Integer> limit,
                                             Optional<String> cursor,
                                             User user) {
        // Create account key from websafe id.
        final Key<Account> authKey = Key.create(user.getUserId());

        // Init query fetch request.
        Query<SavedQuestion> query = ofy()
                .load()
                .type(SavedQuestion.class)
                .ancestor(authKey)
                .order("-" + SavedQuestion.FIELD_CREATED);

        // Fetch items from beginning from cursor.
        query = cursor.isPresent()
                ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
                : query;

        // Limit items.
        query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

        final QueryResultIterator<SavedQuestion> qi = query.iterator();

        ImmutableList.Builder<Key<Question>> builder = ImmutableList.builder();

        while (qi.hasNext()) {
            // Add fetched objects to map. Because cursor iteration needs to be iterated.
            builder.add(qi.next().getSavedQuestionKey());
        }

        Map<Key<Question>, Question> map = ofy().load().keys(builder.build());

        if (!map.isEmpty()) {
            map = QuestionUtil.aggregateCounts(map, questionShardService);
            map = QuestionUtil.aggregateVote(authKey, QuestionSorter.NEWEST, map);
        }

        return CollectionResponse.<Question>builder()
                .setItems(map.values())
                .setNextPageToken(qi.getCursor().toWebSafeString())
                .build();
    }
}