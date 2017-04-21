package com.yoloo.backend.tag;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.shard.Shardable;
import io.reactivex.Observable;
import ix.Ix;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class TagShardService implements Shardable<TagShard, Tag> {

  @Override public Map<Ref<TagShard>, TagShard> createShardMapWithRef(Iterable<Key<Tag>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(postKey -> Observable.range(1, ShardConfig.TAG_SHARD_COUNTER)
            .map(shardNum -> TagShard.builder()
                .id(ShardUtil.generateShardId(postKey, shardNum))
                .posts(0L)
                .build()))
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override public Map<Key<TagShard>, TagShard> createShardMapWithKey(Iterable<Key<Tag>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(postKey -> Observable.range(1, ShardConfig.TAG_SHARD_COUNTER)
            .map(shardNum -> TagShard.builder()
                .id(ShardUtil.generateShardId(postKey, shardNum))
                .posts(0L)
                .build()))
        .toMap(Key::create)
        .blockingGet();
  }

  @Override public Map<Ref<TagShard>, TagShard> createShardMapWithRef(Key<Tag> key) {
    return Observable.range(1, ShardConfig.TAG_SHARD_COUNTER)
        .map(shardNum -> TagShard.builder()
            .id(ShardUtil.generateShardId(key, shardNum))
            .posts(0L)
            .build())
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override public Map<Key<TagShard>, TagShard> createShardMapWithKey(Key<Tag> key) {
    return Observable.range(1, ShardConfig.TAG_SHARD_COUNTER)
        .map(shardNum -> TagShard.builder()
            .id(ShardUtil.generateShardId(key, shardNum))
            .posts(0L)
            .build())
        .toMap(Key::create)
        .blockingGet();
  }

  @Override
  public Key<TagShard> getRandomShardKey(Key<Tag> entityKey) {
    final int shardNum = new Random().nextInt(TagShard.SHARD_COUNT - 1 + 1) + 1;
    return TagShard.createKey(entityKey, shardNum);
  }

  @Override public Observable<List<Tag>> mergeShards(Collection<? extends Tag> entities) {
    return Observable.fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override public Observable<Tag> mergeShards(Tag entity) {
    /*return Observable.fromIterable(entity.getShards())
        .cast(TagShard.class)
        .reduce((s1, s2) -> s1.addValues(s2.getPosts()))
        .map(s -> entity.withPostCount(s.getPosts()))
        .toObservable();*/
    return null;
  }

  public Collection<TagShard> updateShards(Iterable<String> tagNames) {
    Query<Tag> query = ofy().load().type(Tag.class);

    for (String name : tagNames) {
      query = query.filter(Tag.FIELD_NAME + " =", name);
    }

    List<Key<Tag>> tagKeys = query.keys().list();

    List<Key<TagShard>> tagShardKeys = Lists.newArrayListWithCapacity(tagKeys.size());
    tagShardKeys.addAll(Ix.from(tagKeys).map(this::getRandomShardKey).toList());

    Map<Key<TagShard>, TagShard> tagShardMap = ofy().load().keys(tagShardKeys);

    for (Map.Entry<Key<TagShard>, TagShard> entry : tagShardMap.entrySet()) {
      TagShard shard = entry.getValue();
      shard.increasePosts();

      tagShardMap.put(entry.getKey(), shard);
    }

    return tagShardMap.values();
  }
}
