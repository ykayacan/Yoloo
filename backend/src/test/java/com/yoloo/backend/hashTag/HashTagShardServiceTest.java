package com.yoloo.backend.hashTag;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.KeyRange;
import com.yoloo.backend.hashtag.HashTag;
import com.yoloo.backend.hashtag.HashTagCounterShard;
import com.yoloo.backend.hashtag.HashTagShardService;
import com.yoloo.backend.util.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class HashTagShardServiceTest extends TestBase {

    @Override
    public void setUp() {
        super.setUp();

        fact().register(HashTag.class);
        fact().register(HashTagCounterShard.class);
    }

    @Test
    public void testFindShardKeys_single() throws Exception {
        Key<HashTag> hashTagKey = ofy().factory().allocateId(HashTag.class);

        HashTagShardService shardService = HashTagShardService.newInstance();

        List<Key<HashTagCounterShard>> shardKeys = shardService.createShardKeys(hashTagKey);

        assertEquals(HashTagCounterShard.SHARD_COUNT, shardKeys.size());
    }

    @Test
    public void testFindShardKeys_collection() throws Exception {
        KeyRange<HashTag> hashTagKeyRange = ofy().factory().allocateIds(HashTag.class, 5);

        HashTagShardService shardService = HashTagShardService.newInstance();

        List<Key<HashTagCounterShard>> shardKeys = shardService.createShardKeys(hashTagKeyRange);

        assertEquals(HashTagCounterShard.SHARD_COUNT * 5, shardKeys.size());
    }
}
