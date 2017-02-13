package com.yoloo.backend.category;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(staticName = "create")
public class CategoryShardService implements ShardService<Category, CategoryShard> {

  @Override
  public List<Key<CategoryShard>> createShardKeys(Iterable<Key<Category>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .blockingGet();
  }

  @Override
  public List<Key<CategoryShard>> createShardKeys(final Key<Category> entityKey) {
    return Observable
        .range(1, CategoryShard.SHARD_COUNT)
        .map(id -> CategoryShard.createKey(entityKey, id))
        .toList()
        .blockingGet();
  }

  @Override
  public List<CategoryShard> createShards(Iterable<Key<Category>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .blockingGet();
  }

  @Override
  public List<CategoryShard> createShards(final Key<Category> entityKey) {
    return Observable
        .range(1, CategoryShard.SHARD_COUNT)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .blockingGet();
  }

  @Override
  public CategoryShard createShard(Key<Category> entityKey, int shardId) {
    return CategoryShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardId))
        .posts(0)
        .build();
  }

  @Override
  public Key<CategoryShard> getRandomShardKey(Key<Category> entityKey) {
    final int shardNum = new Random().nextInt(CategoryShard.SHARD_COUNT - 1 + 1) + 1;
    return CategoryShard.createKey(entityKey, shardNum);
  }

  public Collection<CategoryShard> updateShards(Iterable<String> categories) {
    Query<Category> query = ofy().load().type(Category.class);

    for (String category : categories) {
      query = query.filter(Category.FIELD_NAME + " =", category);
    }

    List<Key<Category>> categoryKeys = query.keys().list();

    List<Key<CategoryShard>> categoryShardKeys =
        Lists.newArrayListWithCapacity(categoryKeys.size());

    for (Key<Category> key : categoryKeys) {
      categoryShardKeys.add(getRandomShardKey(key));
    }

    Map<Key<CategoryShard>, CategoryShard> map = ofy().load().keys(categoryShardKeys);

    for (CategoryShard ccs : map.values()) {
      ccs.increasePosts();

      map.put(ccs.getKey(), ccs);
    }

    return map.values();
  }
}
