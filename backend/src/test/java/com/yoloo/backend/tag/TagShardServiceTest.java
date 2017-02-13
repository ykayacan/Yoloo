package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.KeyRange;
import com.yoloo.backend.util.TestBase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.yoloo.backend.util.TestObjectifyService.fact;
import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TagShardServiceTest extends TestBase {

    @Override
    public void setUp() {
        super.setUp();

        fact().register(Tag.class);
        fact().register(TagShard.class);
    }

    @Test
    public void testFindShardKeys_single() throws Exception {
        Key<Tag> hashTagKey = ofy().factory().allocateId(Tag.class);

        TagShardService shardService = TagShardService.create();

        List<Key<TagShard>> shardKeys = shardService.createShardKeys(hashTagKey);

        assertEquals(TagShard.SHARD_COUNT, shardKeys.size());
    }

    @Test
    public void testFindShardKeys_collection() throws Exception {
        KeyRange<Tag> hashTagKeyRange = ofy().factory().allocateIds(Tag.class, 5);

        TagShardService shardService = TagShardService.create();

        List<Key<TagShard>> shardKeys = shardService.createShardKeys(hashTagKeyRange);

        assertEquals(TagShard.SHARD_COUNT * 5, shardKeys.size());
    }
}
