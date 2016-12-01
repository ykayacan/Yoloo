package com.yoloo.backend.hashtag;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;

import javax.validation.constraints.Min;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HashTagCounterShard {

    public static final int SHARD_COUNT = ShardConfig.HASHTAG_SHARD_COUNTER;

    /**
     * Websafe hashtagId:shard_num
     */
    @Id
    private String id;

    @Min(0)
    private long questions;

    public Key<HashTagCounterShard> getKey() {
        return Key.create(HashTagCounterShard.class, id);
    }
}
