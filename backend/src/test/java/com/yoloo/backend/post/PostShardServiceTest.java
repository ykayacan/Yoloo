package com.yoloo.backend.post;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.Shardable;
import com.yoloo.backend.util.TestBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static com.yoloo.backend.OfyService.factory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostShardServiceTest extends TestBase {

  @Test public void testCreateShard() {
    Shardable<PostEntity.PostShard, PostEntity> shardable = PostShardService.create();

    Key<Account> accountKey = factory().allocateId(Account.class);
    Key<PostEntity> postKey = factory().allocateId(accountKey, PostEntity.class);

    Map<Ref<PostEntity.PostShard>, PostEntity.PostShard> map = shardable.createShardMapWithRef(postKey);

    assertEquals(ShardConfig.POST_SHARD_COUNTER, map.size());

    for (int i = 1; i <= ShardConfig.POST_SHARD_COUNTER; i++) {
      Ref<PostEntity.PostShard> ref = Ref.create(PostEntity.PostShard.createKey(postKey, i));
      assertTrue(map.containsKey(ref));
    }
  }

  @Test public void testCreateShards() {
    Shardable<PostEntity.PostShard, PostEntity> shardable = PostShardService.create();

    Key<Account> accountKey = factory().allocateId(Account.class);

    Key<PostEntity> postKey1 = factory().allocateId(accountKey, PostEntity.class);
    Key<PostEntity> postKey2 = factory().allocateId(accountKey, PostEntity.class);
    Key<PostEntity> postKey3 = factory().allocateId(accountKey, PostEntity.class);

    List<Key<PostEntity>> keys = new ArrayList<>();
    keys.add(postKey1);
    keys.add(postKey2);
    keys.add(postKey3);

    Map<Ref<PostEntity.PostShard>, PostEntity.PostShard> map = shardable.createShardMapWithRef(keys);

    assertEquals(ShardConfig.POST_SHARD_COUNTER * keys.size(), map.size());

    for (Key<PostEntity> postKey : keys) {
      for (int shardNum = 1; shardNum <= ShardConfig.POST_SHARD_COUNTER; shardNum++) {
        Ref<PostEntity.PostShard> ref = Ref.create(PostEntity.PostShard.createKey(postKey, shardNum));
        assertTrue(map.containsKey(ref));
      }
    }
  }
}
