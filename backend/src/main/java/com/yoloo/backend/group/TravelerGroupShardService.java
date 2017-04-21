package com.yoloo.backend.group;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.shard.Shardable;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class TravelerGroupShardService implements Shardable<TravelerGroupShard, TravelerGroupEntity> {

  @Override
  public Map<Ref<TravelerGroupShard>, TravelerGroupShard> createShardMapWithRef(
      Iterable<Key<TravelerGroupEntity>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(postKey -> Observable.range(1, ShardConfig.GROUP_SHARD_COUNTER)
            .map(shardNum -> TravelerGroupShard.builder()
                .id(ShardUtil.generateShardId(postKey, shardNum))
                .posts(0L)
                .build()))
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override
  public Map<Key<TravelerGroupShard>, TravelerGroupShard> createShardMapWithKey(
      Iterable<Key<TravelerGroupEntity>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(postKey -> Observable.range(1, ShardConfig.GROUP_SHARD_COUNTER)
            .map(shardNum -> TravelerGroupShard.builder()
                .id(ShardUtil.generateShardId(postKey, shardNum))
                .posts(0L)
                .build()))
        .toMap(Key::create)
        .blockingGet();
  }

  @Override
  public Map<Ref<TravelerGroupShard>, TravelerGroupShard> createShardMapWithRef(
      Key<TravelerGroupEntity> key) {
    return Observable.range(1, ShardConfig.GROUP_SHARD_COUNTER)
        .map(shardNum -> TravelerGroupShard.builder()
            .id(ShardUtil.generateShardId(key, shardNum))
            .posts(0L)
            .build())
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override
  public Map<Key<TravelerGroupShard>, TravelerGroupShard> createShardMapWithKey(
      Key<TravelerGroupEntity> key) {
    return Observable.range(1, ShardConfig.GROUP_SHARD_COUNTER)
        .map(shardNum -> TravelerGroupShard.builder()
            .id(ShardUtil.generateShardId(key, shardNum))
            .posts(0L)
            .build())
        .toMap(Key::create)
        .blockingGet();
  }

  @Override
  public Key<TravelerGroupShard> getRandomShardKey(Key<TravelerGroupEntity> entityKey) {
    final int shardNum = new Random().nextInt(ShardConfig.GROUP_SHARD_COUNTER - 1 + 1) + 1;
    return TravelerGroupShard.createKey(entityKey, shardNum);
  }

  @Override
  public Observable<List<TravelerGroupEntity>> mergeShards(Collection<? extends TravelerGroupEntity> entities) {
    return Observable.fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override
  public Observable<TravelerGroupEntity> mergeShards(TravelerGroupEntity entity) {
    return Observable.fromIterable(entity.getShards())
        .cast(TravelerGroupShard.class)
        .reduce((s1, s2) -> s1.addPost(s2.getPosts()))
        .map(shard -> entity.withPostCount(shard.getPosts()))
        .toObservable();
  }

  /*public Collection<TravelerGroupShard> updateShard(TravelerGroupEntity categoryIds) {
    List<Key<TravelerGroupEntity>> categoryKeys = KeyUtil.extractKeysFromIds(categoryIds, ",");

    List<Key<TravelerGroupShard>> categoryShardKeys = new ArrayList<>(categoryKeys.size());

    categoryShardKeys.addAll(Stream.of(categoryKeys).map(this::getRandomShardKey).toList());

    Map<Key<TravelerGroupShard>, TravelerGroupShard> map = ofy().load().keys(categoryShardKeys);

    Stream.of(map).forEach(entry -> {
      entry.getValue().increasePosts();
      map.put(entry.getKey(), entry.getValue());
    });

    return map.values();
  }*/
}
