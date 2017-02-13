package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class TagShardService implements ShardService<Tag, TagShard> {

  @Override
  public List<Key<TagShard>> createShardKeys(Iterable<Key<Tag>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public List<Key<TagShard>> createShardKeys(final Key<Tag> entityKey) {
    return Observable
        .range(1, ShardConfig.TAG_SHARD_COUNTER)
        .map(id -> TagShard.createKey(entityKey, id))
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public List<TagShard> createShards(Iterable<Key<Tag>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public List<TagShard> createShards(final Key<Tag> entityKey) {
    return Observable
        .range(1, ShardConfig.TAG_SHARD_COUNTER)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public TagShard createShard(Key<Tag> entityKey, int shardNum) {
    return TagShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardNum))
        .posts(0)
        .build();
  }

  @Override
  public Key<TagShard> getRandomShardKey(Key<Tag> entityKey) {
    final int shardNum = new Random().nextInt(TagShard.SHARD_COUNT - 1 + 1) + 1;
    return TagShard.createKey(entityKey, shardNum);
  }

  public Collection<TagShard> updateShards(Iterable<String> tagNames) {
    Query<Tag> query = ofy().load().type(Tag.class);

    for (String name : tagNames) {
      query = query.filter(Tag.FIELD_NAME + " =", name);
    }

    List<Key<Tag>> tagKeys = query.keys().list();

    List<Key<TagShard>> tagShardKeys = new ArrayList<>(tagKeys.size());
    for (Key<Tag> key : tagKeys) {
      tagShardKeys.add(getRandomShardKey(key));
    }

    Map<Key<TagShard>, TagShard> tagShardMap = ofy().load().keys(tagShardKeys);

    for (Map.Entry<Key<TagShard>, TagShard> entry : tagShardMap.entrySet()) {
      TagShard shard = entry.getValue();
      shard.increasePosts();

      tagShardMap.put(entry.getKey(), shard);
    }

    return tagShardMap.values();
  }
}
