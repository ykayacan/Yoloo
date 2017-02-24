package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.KeyRange;
import com.yoloo.backend.util.TestBase;
import java.util.Map;
import org.junit.Test;

import static com.yoloo.backend.util.TestObjectifyService.ofy;
import static org.junit.Assert.assertEquals;

public class TagShardServiceTest extends TestBase {

  @Test public void testFindShardKeys_single() throws Exception {
    Key<Tag> hashTagKey = ofy().factory().allocateId(Tag.class);

    TagShardService shardService = TagShardService.create();

    Map<Key<TagShard>, TagShard> map = shardService.createShardMapWithKey(hashTagKey);

    assertEquals(TagShard.SHARD_COUNT, map.keySet().size());
  }

  @Test public void testFindShardKeys_collection() throws Exception {
    KeyRange<Tag> hashTagKeyRange = ofy().factory().allocateIds(Tag.class, 5);

    TagShardService shardService = TagShardService.create();

    Map<Key<TagShard>, TagShard> map = shardService.createShardMapWithKey(hashTagKeyRange);

    assertEquals(TagShard.SHARD_COUNT * 5, map.keySet().size());
  }
}
