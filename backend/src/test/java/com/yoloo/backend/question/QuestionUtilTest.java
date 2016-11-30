package com.yoloo.backend.question;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.KeyRange;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.GaeTestBase;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;
import com.yoloo.backend.vote.Vote;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.yoloo.backend.OfyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class QuestionUtilTest extends GaeTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void registerClasses(ImmutableList.Builder<Class<?>> builder) {
        builder.add(Question.class)
                .add(Account.class)
                .add(QuestionCounterShard.class)
                .add(Category.class)
                .add(Vote.class);
    }

    @Test
    public void testIfCountAggregationIsCorrectForSinglePost() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        Key<Question> postKey = ofy().factory().allocateId(userKey, Question.class);

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        Question post = createPost(userKey, postKey);

        builder.add(post).addAll(QuestionShardService.newInstance().createShards(postKey));

        ofy().save().entities(builder.build()).now();

        int shardNum = new Random().nextInt(QuestionCounterShard.SHARD_COUNT - 1 + 1) + 1;

        QuestionCounterShard shard =
                ofy().load().type(QuestionCounterShard.class)
                        .id(QuestionUtil.createShardId(postKey, shardNum)).now();
        shard.addVotes(10);

        ofy().save().entity(shard).now();

        Map<Key<Question>, Question> map = Maps.newLinkedHashMap();
        map.put(postKey, post);

        map = QuestionUtil.aggregateCounts(map, QuestionShardService.newInstance());

        assertEquals(10, map.get(postKey).getVotes());
    }

    @Test
    public void testMinAndMaxDatedPostPairByHotOrder() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        KeyRange<Question> postKeyRange = ofy().factory().allocateIds(userKey, Question.class, 6);

        List<Question> list = new ArrayList<>();

        for (Key<Question> postKey : postKeyRange) {
            list.add(createPost(userKey, postKey));

            Thread.sleep(50);
        }

        Collections.shuffle(list);

        Pair<Question, Question> pair =
                QuestionUtil.getPostPairOrderedByDate(list, QuestionSorter.HOT);

        assertTrue(pair.first.getCreated().isBefore(pair.second.getCreated()));
    }

    @Test
    public void testMinAndMaxDatedPostPairByNewestOrder() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        KeyRange<Question> postKeyRange = ofy().factory().allocateIds(userKey, Question.class, 6);

        List<Question> list = new ArrayList<>();

        for (Key<Question> postKey : postKeyRange) {
            list.add(createPost(userKey, postKey));

            Thread.sleep(50);
        }

        Pair<Question, Question> pair =
                QuestionUtil.getPostPairOrderedByDate(list, QuestionSorter.NEWEST);

        assertTrue(pair.first.getCreated().isBefore(pair.second.getCreated()));
    }

    @Test
    public void testMinAndMaxDatedPostPairByUnansweredOrder() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        KeyRange<Question> postKeyRange = ofy().factory().allocateIds(userKey, Question.class, 6);

        List<Question> list = new ArrayList<>();

        for (Key<Question> postKey : postKeyRange) {
            list.add(createPost(userKey, postKey));

            Thread.sleep(50);
        }

        Collections.shuffle(list);

        Pair<Question, Question> pair =
                QuestionUtil.getPostPairOrderedByDate(list, QuestionSorter.UNANSWERED);

        assertTrue(pair.first.getCreated().isBefore(pair.second.getCreated()));
    }

    @Test
    public void testShowVoteDirectionOfAuthenticatedUser() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        Key<Question> postKey = ofy().factory().allocateId(userKey, Question.class);

        ImmutableList.Builder<Object> builder = ImmutableList.builder();

        Question post = createPost(userKey, postKey);
        Vote vote = createVote(userKey, postKey);

        builder.add(post).add(vote);

        ofy().save().entities(builder.build()).now();

        Map<Key<Question>, Question> questionMap = Maps.newLinkedHashMap();
        questionMap.put(postKey, post);

        questionMap = QuestionUtil.aggregateVote(userKey, QuestionSorter.NEWEST, questionMap);

        assertTrue(questionMap.get(postKey).getDir().compareTo(Vote.Direction.UP) == 0);
    }

    private Vote createVote(Key<Account> userKey, Key<Question> postKey) {
        return Vote.builder()
                .id(postKey.toWebSafeString())
                .parentUserKey(userKey)
                .dir(Vote.Direction.UP)
                .build();
    }

    private Question createPost(Key<Account> userKey, Key<Question> postKey) {
        return Question.builder()
                .id(postKey.getId())
                .parentUserKey(userKey)
                .acceptedComment(null)
                .avatarUrl(new Link("test"))
                .username("testUser")
                .bounty(0)
                .comments(0)
                .votes(0)
                .firstComment(false)
                .content("demo content")
                .dir(Vote.Direction.DEFAULT)
                .created(DateTime.now())
                .build();
    }
}
