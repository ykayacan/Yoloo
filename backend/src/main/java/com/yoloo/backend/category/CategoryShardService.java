package com.yoloo.backend.category;

import com.google.common.collect.ImmutableSet;

import com.googlecode.objectify.Key;

import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.category.CategoryCounterShard.SHARD_COUNT;

@RequiredArgsConstructor(staticName = "newInstance")
public class CategoryShardService {

    public ImmutableSet<CategoryCounterShard> createShards(final Key<Category> key) {
        return Observable.range(1, SHARD_COUNT)
                .map(new Function<Integer, CategoryCounterShard>() {
                    @Override
                    public CategoryCounterShard apply(Integer shardId) throws Exception {
                        return createShard(key, shardId);
                    }
                })
                .to(new Function<Observable<CategoryCounterShard>, ImmutableSet<CategoryCounterShard>>() {
                    @Override
                    public ImmutableSet<CategoryCounterShard> apply(Observable<CategoryCounterShard> o)
                            throws Exception {
                        return ImmutableSet.copyOf(o.toList().blockingGet());
                    }
                });
    }

    public CategoryCounterShard createShard(Key<Category> key, int shardId) {
        return CategoryCounterShard.builder()
                .id(CategoryUtil.createShardId(key, shardId))
                .questions(0)
                .build();
    }

    public ImmutableSet<Key<CategoryCounterShard>> getShardKeys(Collection<Key<Category>> keys) {
        return Observable.fromIterable(keys)
                .map(new Function<Key<Category>, List<Key<CategoryCounterShard>>>() {
                    @Override
                    public List<Key<CategoryCounterShard>> apply(final Key<Category> key)
                            throws Exception {
                        return Observable.range(1, SHARD_COUNT)
                                .map(new Function<Integer, Key<CategoryCounterShard>>() {
                                    @Override
                                    public Key<CategoryCounterShard> apply(Integer shardId)
                                            throws Exception {
                                        return Key.create(
                                                CategoryCounterShard.class,
                                                CategoryUtil.createShardId(key, shardId));
                                    }
                                }).toList().blockingGet();
                    }
                })
                .toList()
                .to(new Function<Single<List<List<Key<CategoryCounterShard>>>>,
                        ImmutableSet<Key<CategoryCounterShard>>>() {
                    @Override
                    public ImmutableSet<Key<CategoryCounterShard>> apply(
                            Single<List<List<Key<CategoryCounterShard>>>> listSingle)
                            throws Exception {
                        ImmutableSet.Builder<Key<CategoryCounterShard>> builder =
                                ImmutableSet.builder();

                        for (List<Key<CategoryCounterShard>> keys : listSingle.blockingGet()) {
                            builder = builder.addAll(keys);
                        }

                        return builder.build();
                    }
                });
    }
}
