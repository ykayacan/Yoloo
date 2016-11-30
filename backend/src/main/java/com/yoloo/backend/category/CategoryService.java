package com.yoloo.backend.category;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import com.googlecode.objectify.Key;
import com.yoloo.backend.question.Question;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.category.CategoryCounterShard.SHARD_COUNT;

@RequiredArgsConstructor(staticName = "newInstance")
public class CategoryService {

    public Category create(String name) {
        return Category.builder()
                .name(name)
                .rank(0)
                .build();
    }

    public ImmutableSet<Key<Category>> getCategoryKeys(Collection<String> websafeIds) {
        return Observable.fromIterable(websafeIds)
                .map(new Function<String, Key<Category>>() {
                    @Override
                    public Key<Category> apply(String s) throws Exception {
                        return Key.create(s);
                    }
                })
                .toList()
                .to(new Function<Single<List<Key<Category>>>, ImmutableSet<Key<Category>>>() {
                    @Override
                    public ImmutableSet<Key<Category>> apply(Single<List<Key<Category>>> listSingle)
                            throws Exception {
                        return ImmutableSet.copyOf(listSingle.blockingGet());
                    }
                });
    }

    public Question setCategories(Question question, Collection<String> websafeIds) {
        ImmutableSet<Key<Category>> categoryKeys = getCategoryKeys(websafeIds);

        Collection<Category> categories = ofy().load().keys(categoryKeys).values();
        Map<Key<Category>, String> categoryMap = Maps.newHashMap();
        for (Category category : categories) {
            categoryMap.put(category.getKey(), category.getName());
        }

        return question.withCategoryKeys(categoryMap.keySet())
                .withCategories(ImmutableSet.copyOf(categoryMap.values()));
    }

    public Key<CategoryCounterShard> getRandomShardKey(Key<Category> categoryKey) {
        final int shardNum = new Random().nextInt(SHARD_COUNT - 1 + 1) + 1;
        return Key.create(CategoryCounterShard.class,
                CategoryUtil.createShardId(categoryKey, shardNum));
    }

    public ImmutableSet<Key<CategoryCounterShard>> getRandomShardKeys(
            Collection<Key<Category>> categoryKeys) {
        final int shardNum = new Random().nextInt(SHARD_COUNT - 1 + 1) + 1;
        return Observable.fromIterable(categoryKeys)
                .map(new Function<Key<Category>, Key<CategoryCounterShard>>() {
                    @Override
                    public Key<CategoryCounterShard> apply(Key<Category> categoryKey)
                            throws Exception {
                        return Key.create(CategoryCounterShard.class,
                                CategoryUtil.createShardId(categoryKey, shardNum));
                    }
                })
                .toList()
                .to(new Function<Single<List<Key<CategoryCounterShard>>>,
                        ImmutableSet<Key<CategoryCounterShard>>>() {
                    @Override
                    public ImmutableSet<Key<CategoryCounterShard>> apply(
                            Single<List<Key<CategoryCounterShard>>> listSingle)
                            throws Exception {
                        return ImmutableSet.copyOf(listSingle.blockingGet());
                    }
                });
    }
}
