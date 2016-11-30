package com.yoloo.backend.category;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Cache(expirationSeconds = 60)
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryCounterShard {

    public static final int SHARD_COUNT = ShardConfig.CATEGORY_SHARD_COUNTER;

    /**
     * Websafe categoryId:shard_num
     */
    @Id
    private String id;

    private long questions;

    public Key<CategoryCounterShard> getKey() {
        return Key.create(CategoryCounterShard.class, id);
    }

    public void increaseQuestions() {
        this.questions++;
    }

    public void decreaseQuestions() {
        this.questions--;
    }
}
