package com.yoloo.backend.category;

import com.annimon.stream.Stream;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.shard.Shardable;
import com.yoloo.backend.util.KeyUtil;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(staticName = "create")
public class CategoryShardService implements Shardable<CategoryShard, Category> {

  @Override public Map<Ref<CategoryShard>, CategoryShard> createShardMapWithRef(
      Iterable<Key<Category>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(postKey -> Observable.range(1, ShardConfig.CATEGORY_SHARD_COUNTER)
            .map(shardNum -> CategoryShard.builder()
                .id(ShardUtil.generateShardId(postKey, shardNum))
                .posts(0L)
                .build()))
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override public Map<Key<CategoryShard>, CategoryShard> createShardMapWithKey(
      Iterable<Key<Category>> keys) {
    return Observable.fromIterable(keys)
        .flatMap(postKey -> Observable.range(1, ShardConfig.CATEGORY_SHARD_COUNTER)
            .map(shardNum -> CategoryShard.builder()
                .id(ShardUtil.generateShardId(postKey, shardNum))
                .posts(0L)
                .build()))
        .toMap(Key::create)
        .blockingGet();
  }

  @Override public Map<Ref<CategoryShard>, CategoryShard> createShardMapWithRef(Key<Category> key) {
    return Observable.range(1, ShardConfig.CATEGORY_SHARD_COUNTER)
        .map(shardNum -> CategoryShard.builder()
            .id(ShardUtil.generateShardId(key, shardNum))
            .posts(0L)
            .build())
        .toMap(Ref::create)
        .blockingGet();
  }

  @Override public Map<Key<CategoryShard>, CategoryShard> createShardMapWithKey(Key<Category> key) {
    return Observable.range(1, ShardConfig.CATEGORY_SHARD_COUNTER)
        .map(shardNum -> CategoryShard.builder()
            .id(ShardUtil.generateShardId(key, shardNum))
            .posts(0L)
            .build())
        .toMap(Key::create)
        .blockingGet();
  }

  @Override public Key<CategoryShard> getRandomShardKey(Key<Category> entityKey) {
    final int shardNum = new Random().nextInt(ShardConfig.CATEGORY_SHARD_COUNTER - 1 + 1) + 1;
    return CategoryShard.createKey(entityKey, shardNum);
  }

  @Override public Observable<List<Category>> mergeShards(Collection<? extends Category> entities) {
    return Observable.fromIterable(entities)
        .flatMap(this::mergeShards)
        .toList(entities.size() == 0 ? 1 : entities.size())
        .toObservable();
  }

  @Override public Observable<Category> mergeShards(Category entity) {
    return Observable.fromIterable(entity.getShards())
        .cast(CategoryShard.class)
        .reduce((s1, s2) -> s1.addPost(s2.getPosts()))
        .map(shard -> entity.withPosts(shard.getPosts()))
        .toObservable();
  }

  public Collection<CategoryShard> updateShards(String categoryIds) {
    List<Key<Category>> categoryKeys = KeyUtil.extractKeysFromIds(categoryIds, ",");

    List<Key<CategoryShard>> categoryShardKeys =
        Lists.newArrayListWithCapacity(categoryKeys.size());

    categoryShardKeys.addAll(Stream.of(categoryKeys).map(this::getRandomShardKey).toList());

    Map<Key<CategoryShard>, CategoryShard> map = ofy().load().keys(categoryShardKeys);

    Stream.of(map).forEach(entry -> {
      entry.getValue().increasePosts();
      map.put(entry.getKey(), entry.getValue());
    });

    return map.values();
  }
}
