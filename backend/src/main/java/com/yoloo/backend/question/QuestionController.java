package com.yoloo.backend.question;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountCounterShard;
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.category.CategoryCounterShard;
import com.yoloo.backend.category.CategoryService;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.hashtag.HashTagService;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;
import com.yoloo.backend.util.StringUtil;
import com.yoloo.backend.vote.Vote;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public final class QuestionController extends Controller {

    private static final Logger logger =
            Logger.getLogger(QuestionController.class.getName());

    /**
     * Maximum number of questions to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    @NonNull
    private QuestionService questionService;

    @NonNull
    private QuestionShardService questionShardService;

    @NonNull
    private CommentService commentService;

    @NonNull
    private CommentShardService commentShardService;

    @NonNull
    private HashTagService hashTagService;

    @NonNull
    private CategoryService categoryService;

    @NonNull
    private AccountService accountService;

    @NonNull
    private AccountShardService accountShardService;

    @NonNull
    private GamificationService gamificationService;

    @NonNull
    private MediaService mediaService;

    @NonNull
    private NotificationService notificationService;

    /**
     * Get question.
     *
     * @param websafeQuestionId the websafe question id
     * @param user              the user
     * @return the question
     */
    public Question get(String websafeQuestionId, User user) {
        // Create account key from websafe id.
        final Key<Account> accountKey = Key.create(user.getUserId());

        // Create question key from websafe id.
        final Key<Question> questionKey = Key.create(websafeQuestionId);

        // Create vote key from websafe id.
        final Key<Vote> voteKey =
                Key.create(accountKey, Vote.class, questionKey.toWebSafeString());

        // Fetch question.
        Question question = ofy().load().key(questionKey).now();

        question = QuestionUtil.aggregateCounts(question);

        try {
            Vote vote = ofy().load().key(voteKey).safe();
            return question.withDir(vote.getDir());
        } catch (NotFoundException e) {
            return question;
        }
    }

    /**
     * Add question.
     *
     * @param wrapper the wrapper
     * @return the question
     */
    public Question add(QuestionWrapper wrapper, User user) {
        // Create user key from user id.
        final Key<Account> authKey = Key.create(user.getUserId());

        // Get a random shard key.
        final Key<AccountCounterShard> accountCounterShardKey =
                accountShardService.getRandomShardKey(authKey);

        // Make a batch load.
        Map<Key<Object>, Object> map = ofy().load().keys(authKey, accountCounterShardKey);

        // Create a new post object from given inputs.
        @SuppressWarnings("SuspiciousMethodCalls")
        Question question =
                questionService.create((Account) map.get(authKey), wrapper, questionShardService);

        // Create a list of shard entities for given post object.
        List<QuestionCounterShard> shards =
                questionShardService.createShards(question.getKey());

        question = categoryService
                .setCategories(question, StringUtil.splitToSet(wrapper.getCategoryIds(), ","));

        ImmutableSet<Key<CategoryCounterShard>> categoryShardKeys = categoryService
                .getRandomShardKeys(question.getCategoryKeys());

        Collection<CategoryCounterShard> categoryCounterShards = ofy().load()
                .keys(categoryShardKeys).values();

        // TODO: 28.11.2016 Increase counter shards.

        // Add generated hashTags to save list.
        //ImmutableSet<HashTag> hashTags = hashTagService.createHashTag(wrapper.getHashTags());

        // Add updated account shard counter to save list.
        @SuppressWarnings("SuspiciousMethodCalls")
        AccountCounterShard shard = accountShardService.updateCounter(
                (AccountCounterShard) map.get(accountCounterShardKey),
                AccountShardService.UpdateType.POST_UP);

        // Immutable helper list object to save all entities in a single db write.
        // For each single object use builder.add() method.
        // For each list object use builder.addAll() method.
        ImmutableList<Object> saveList = ImmutableList.builder()
                .add(question)
                .addAll(shards)
                .add(shard)
                .build();

        // TODO: 27.11.2016 Implement media.
        // TODO: 26.11.2016 Check gamification rules.
        // TODO: 26.11.2016 Send post to friends of user's timeline.
        // TODO: 28.11.2016 Implement category service.

        ofy().save().entities(saveList).now();

        return question;
    }

    /**
     * Update question.
     *
     * @param wrapper the wrapper
     * @param user
     * @return the question
     */
    public Question update(QuestionWrapper wrapper, User user) {
        // Create question key from websafe question id.
        final Key<Question> questionKey = Key.create(wrapper.getWebsafeQuestionId());

        Question question = ofy().load().key(questionKey).now();

        question = questionService.update(question, wrapper);

        // TODO: 26.11.2016 Check gamification rules.

        ofy().save().entity(question).now();

        return question;
    }

    /**
     * Remove question.
     *
     * @param websafeQuestionId the websafe question id
     * @param user              the user
     */
    public void delete(String websafeQuestionId, User user) {
        // Create account key from websafe id.
        final Key<Account> accountKey = Key.create(user.getUserId());

        // Create question key from websafe id.
        final Key<Question> questionKey = Key.create(websafeQuestionId);

        Key<AccountCounterShard> shardKey = accountShardService.getRandomShardKey(accountKey);
        final AccountCounterShard shard = ofy().load().key(shardKey).now();
        shard.decreaseQuestions();

        // TODO: 27.11.2016 Change this operations to a push queue.

        List<Key<Comment>> commentKeys = questionService.getCommentKeys(questionKey);

        final ImmutableList<Key<?>> deleteList = ImmutableList.<Key<?>>builder()
                .addAll(commentShardService.createShardKeys(commentKeys))
                .addAll(commentService.getVoteKeys(commentKeys))
                .addAll(commentKeys)
                .addAll(questionService.getVoteKeys(questionKey))
                .addAll(questionShardService.createShardKeys(questionKey))
                .add(questionKey)
                .build();

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().defer().save().entity(shard);
                ofy().defer().delete().keys(deleteList);
            }
        });
    }

    /**
     * List collection response.
     *
     * @param sorter the sorter
     * @param limit  the limit
     * @param cursor the cursor
     * @param user   the user
     * @return the collection response
     */
    public CollectionResponse<Question> list(Optional<QuestionSorter> sorter,
                                             Optional<Integer> limit,
                                             Optional<String> cursor,
                                             User user) {
        // Create account key from websafe id.
        final Key<Account> authKey = Key.create(user.getUserId());

        // If sorter parameter is null, default sort strategy is "NEWEST".
        QuestionSorter questionSorter = sorter.or(QuestionSorter.NEWEST);

        // Init query fetch request.
        Query<Question> query = ofy().load().type(Question.class);

        // Sort by post sorter then edit query.
        query = QuestionSorter.sort(query, questionSorter);

        // Fetch items from beginning from cursor.
        query = cursor.isPresent()
                ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
                : query;

        // Limit items.
        query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

        final QueryResultIterator<Question> qi = query.iterator();

        Map<Key<Question>, Question> map = Maps.newLinkedHashMap();

        while (qi.hasNext()) {
            // Add fetched objects to map. Because cursor iteration needs to be iterated.
            Question item = qi.next();
            map.put(item.getKey(), item);
        }

        if (!map.isEmpty()) {
            map = QuestionUtil.aggregateCounts(map, questionShardService);
            map = QuestionUtil.aggregateVote(authKey, questionSorter, map);
        }

        return CollectionResponse.<Question>builder()
                .setItems(map.values())
                .setNextPageToken(qi.getCursor().toWebSafeString())
                .build();
    }
}