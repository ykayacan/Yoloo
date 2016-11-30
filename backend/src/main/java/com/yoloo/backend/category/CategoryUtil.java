package com.yoloo.backend.category;

import com.google.common.collect.ImmutableSet;

import com.googlecode.objectify.Key;

import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryUtil {

    public static Map<Key<Category>, Category> aggregateCounts(
            Map<Key<Category>, Category> categoryMap,
            CategoryShardService service) {

        ImmutableSet<Key<CategoryCounterShard>> shardKeys = 
                service.getShardKeys(categoryMap.keySet());
        final Map<Key<CategoryCounterShard>, CategoryCounterShard> shardMap =
                ofy().load().keys(shardKeys);

        for (Category category : categoryMap.values()) {
            long questions = 0L;

            for (int i = 1; i <= CategoryCounterShard.SHARD_COUNT; i++) {
                Key<CategoryCounterShard> shardKey =
                        Key.create(CategoryCounterShard.class, createShardId(category.getKey(), i));

                if (shardMap.containsKey(shardKey)) {
                    CategoryCounterShard shard = shardMap.get(shardKey);
                    questions += shard.getQuestions();
                }
            }

            categoryMap.put(category.getKey(), category.withQuestions(questions));
        }

        return categoryMap;
    }

    public static String createShardId(Key<Category> categoryKey, int shardNum) {
        return categoryKey.toWebSafeString() + ":" + String.valueOf(shardNum);
    }
}
