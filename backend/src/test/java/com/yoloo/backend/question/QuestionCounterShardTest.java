package com.yoloo.backend.question;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.KeyRange;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.GaeTestBase;
import com.yoloo.backend.vote.Vote;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class QuestionCounterShardTest extends GaeTestBase {

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
                .add(Vote.class);
    }

    @Test
    public void testCreateQuestionShards() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        Key<Question> postKey = ofy().factory().allocateId(userKey, Question.class);

        ImmutableList<QuestionCounterShard> shards =
                QuestionShardService.newInstance().createShards(postKey);

        assertEquals(5, shards.size());

        String websafeString = postKey.toWebSafeString();

        for (int i = 1; i <= QuestionCounterShard.SHARD_COUNT; i++) {
            assertEquals(websafeString + ":" + String.valueOf(i), shards.get(i - 1).getId());
        }
    }

    @Test
    public void testIsKeyForForumCounterShardFromAggregateItemCorrect_multiple() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        KeyRange<Question> postKeyRange = ofy().factory().allocateIds(userKey, Question.class, 20);

        ImmutableList<Key<Question>> postKeys = ImmutableList.copyOf(postKeyRange);

        /*ImmutableList<Key<QuestionCounterShard>> shardKeys =
                QuestionShardService.newInstance().getShardKeys(postKeys);*/

        //assertEquals(100, shardKeys.size());
    }

    @Test
    public void testIsKeyForForumCounterShardFromAggregateItemCorrect_single() throws Exception {
        Key<Account> userKey = ofy().factory().allocateId(Account.class);
        Key<Question> questionKey = ofy().factory().allocateId(userKey, Question.class);

        ImmutableList<Key<QuestionCounterShard>> shardKeys =
                QuestionShardService.newInstance().getShardKeys(questionKey);

        assertEquals(5, shardKeys.size());
    }
}
