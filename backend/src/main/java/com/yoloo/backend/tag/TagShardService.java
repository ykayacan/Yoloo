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
public class TagShardService implements ShardService<Tag, TagCounterShard> {

  @Override
  public List<Key<TagCounterShard>> createShardKeys(Iterable<Key<Tag>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public List<Key<TagCounterShard>> createShardKeys(final Key<Tag> entityKey) {
    return Observable
        .range(1, ShardConfig.TAG_SHARD_COUNTER)
        .map(id -> TagCounterShard.createKey(entityKey, id))
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public List<TagCounterShard> createShards(Iterable<Key<Tag>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public List<TagCounterShard> createShards(final Key<Tag> entityKey) {
    return Observable
        .range(1, ShardConfig.TAG_SHARD_COUNTER)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .cache()
        .blockingGet();
  }

  @Override
  public TagCounterShard createShard(Key<Tag> entityKey, int shardNum) {
    return TagCounterShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardNum))
        .questions(0)
        .build();
  }

  @Override
  public Key<TagCounterShard> getRandomShardKey(Key<Tag> entityKey) {
    final int shardNum = new Random().nextInt(TagCounterShard.SHARD_COUNT - 1 + 1) + 1;
    return TagCounterShard.createKey(entityKey, shardNum);
  }

  public Collection<TagCounterShard> updateShards(Iterable<String> tagNames) {
    Query<Tag> query = ofy().load().type(Tag.class);

    for (String name : tagNames) {
      query = query.filter(Tag.FIELD_NAME + " =", name);
    }

    List<Key<Tag>> tagKeys = query.keys().list();

    List<Key<TagCounterShard>> tagShardKeys = new ArrayList<>(tagKeys.size());
    for (Key<Tag> key : tagKeys) {
      tagShardKeys.add(getRandomShardKey(key));
    }

    Map<Key<TagCounterShard>, TagCounterShard> tagShardMap = ofy().load().keys(tagShardKeys);

    for (Map.Entry<Key<TagCounterShard>, TagCounterShard> entry : tagShardMap.entrySet()) {
      TagCounterShard shard = entry.getValue();
      shard.increaseQuestions();

      tagShardMap.put(entry.getKey(), shard);
    }

    return tagShardMap.values();
  }
}
