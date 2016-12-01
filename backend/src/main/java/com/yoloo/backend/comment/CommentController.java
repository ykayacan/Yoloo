package com.yoloo.backend.comment;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.question.QuestionService;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.vote.Votable;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public class CommentController extends Controller {

    private static final Logger logger =
            Logger.getLogger(CommentController.class.getName());

    /**
     * Maximum number of comments to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    @NonNull
    private CommentService commentService;

    @NonNull
    private CommentShardService commentShardService;

    @NonNull
    private QuestionService questionService;

    @NonNull
    private QuestionShardService questionShardService;

    @NonNull
    private GamificationService gamificationService;

    @NonNull
    private NotificationService notificationService;

    /**
     * Get comment.
     *
     * @param websafeCommentId the websafe comment id
     * @param user             the user
     * @return the comment
     */
    public Comment get(String websafeCommentId, User user) {
        final Key<Account> authKey = Key.create(user.getUserId());
        final Key<Comment> commentKey = Key.create(websafeCommentId);

        Comment comment = ofy().load().key(commentKey).now();

        comment = CommentUtil.aggregateCounts(comment);
        comment = CommentUtil.aggregateVote(authKey, comment);

        return comment;
    }

    /**
     * Add comment.
     *
     * @param websafeQuestionId the websafe question id
     * @param content           the content
     * @param user              the user
     * @return the comment
     */
    public Comment add(String websafeQuestionId, String content, User user) {
        // Create user key from user id.
        final Key<Account> userKey = Key.create(user.getUserId());

        // Create post key from websafe id.
        final Key<Question> questionKey = Key.create(websafeQuestionId);

        // Get a random shard key.
        final Key<QuestionCounterShard> questionShardKey =
                questionShardService.getRandomShardKey(questionKey);

        // Make a batch load.
        Map<Key<Object>, Object> map = ofy().load()
                .keys(userKey, questionKey, questionShardKey);

        // Immutable helper list object to save all entities in a single db write.
        // For each single object use builder.add() method.
        // For each list object use builder.addAll() method.
        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        // Create a new post object from given inputs.
        //noinspection SuspiciousMethodCalls
        Comment comment = commentService
                .create((Account) map.get(userKey), questionKey, content, commentShardService);
        builder.add(comment);

        // Create a list of new shard entities for given comment.
        List<CommentCounterShard> shards =
                commentShardService.createShards(comment.getKey());
        builder.addAll(shards);

        // Get counter shard from map.
        //noinspection SuspiciousMethodCalls
        QuestionCounterShard shard = (QuestionCounterShard) map.get(questionShardKey);

        // Increase total comment number.
        shard.increaseComments();
        builder.add(shard);

        //noinspection SuspiciousMethodCalls
        Question question = (Question) map.get(questionKey);

        if (!question.isFirstComment()) {
            question = question.withFirstComment(true);
            builder.add(question);

            // TODO: 27.11.2016 Gamification.
        }

        ofy().save().entities(builder.build()).now();

        // TODO: 27.11.2016 Implement notification service.

        return comment;
    }

    /**
     * Update comment.
     *
     * @param websafeQuestionId the websafe question id
     * @param websafeCommentId  the websafe comment id
     * @param content           the content
     * @param accepted          the accepted
     * @param user              the user
     * @return the comment
     */
    public Comment update(String websafeQuestionId, String websafeCommentId,
                          Optional<String> content, Optional<Boolean> accepted, User user) {
        // Create user key from user id.
        final Key<Account> userKey = Key.create(user.getUserId());

        // Create question key from websafe id.
        final Key<Question> questionKey = Key.create(websafeQuestionId);

        // Create comment key from websafe id.
        final Key<Comment> commentKey = Key.create(websafeCommentId);

        // Make a batch load.
        @SuppressWarnings("unchecked")
        Map<Key<Votable>, Votable> map = ofy().load().keys(questionKey, commentKey);

        @SuppressWarnings("SuspiciousMethodCalls")
        Comment comment = (Comment) map.get(commentKey);
        comment = commentService.update(comment, content, accepted);

        @SuppressWarnings("SuspiciousMethodCalls")
        Question question = (Question) map.get(questionKey);
        Pair<Question, Comment> pair = commentService.accept(question, comment, accepted);

        // Immutable helper list object to save all entities in a single db write.
        ImmutableList<Object> saveList = ImmutableList.builder()
                .add(pair.first)
                .add(pair.second)
                .build();

        ofy().save().entities(saveList).now();

        // TODO: 27.11.2016 Send bounty to taker.
        // TODO: 27.11.2016 Implement notification service.

        return comment;
    }

    /**
     * Remove.
     *
     * @param websafeQuestionId the websafe question id
     * @param websafeCommentId  the websafe comment id
     * @param user              the user
     */
    public void delete(String websafeQuestionId, String websafeCommentId, User user) {
        // Create user key from user id.
        //final Key<Account> userKey = Key.createHashTag(user.getUserId());

        // Create comment key from websafe id.
        final Key<Comment> commentKey = Key.create(websafeCommentId);

        // Immutable helper list object to save all entities in a single db write.
        final ImmutableList<Key<?>> saveList = ImmutableList.<Key<?>>builder()
                .add(commentKey)
                .addAll(commentShardService.createShardKeys(commentKey))
                .addAll(commentService.getVoteKeys(commentKey))
                .build();

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().delete().keys(saveList);
            }
        });
    }

    /**
     * List collection response.
     *
     * @param websafeQuestionId the websafe question id
     * @param cursor            the cursor
     * @param limit             the limit
     * @param user              the user
     * @return the collection response
     */
    public CollectionResponse<Comment> list(String websafeQuestionId,
                                            Optional<String> cursor,
                                            Optional<Integer> limit,
                                            User user) {
        final Key<Account> authKey = Key.create(user.getUserId());
        final Key<Question> questionKey = Key.create(websafeQuestionId);

        Query<Comment> query = ofy().load().type(Comment.class);
        query = query.filter(Comment.FIELD_QUESTION_KEY + " =", questionKey);
        query = query.order("-" + Comment.FIELD_CREATED);
        query = cursor.isPresent()
                ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
                : query;
        query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

        final QueryResultIterator<Comment> iterator = query.iterator();

        Map<Key<Comment>, Comment> commentMap = Maps.newLinkedHashMap();

        while (iterator.hasNext()) {
            Comment item = iterator.next();
            commentMap.put(item.getKey(), item);
        }

        if (!commentMap.isEmpty()) {
            commentMap = CommentUtil.aggregateCounts(commentMap, commentShardService);
            commentMap = CommentUtil.aggregateVote(authKey, commentMap);
        }

        return CollectionResponse.<Comment>builder()
                .setItems(commentMap.values())
                .setNextPageToken(iterator.getCursor().toWebSafeString())
                .build();
    }
}
