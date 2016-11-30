package com.yoloo.backend.hashtag;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.Min;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
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
     * Websafe questionId:shard_num
     */
    @Id
    private String id;

    @Min(0)
    private long count;

    public Key<HashTagCounterShard> getKey() {
        return Key.create(HashTagCounterShard.class, id);
    }

    public static ImmutableList<HashTagCounterShard> createShards(final Key<HashTag> hashTagKey) {
        return Observable.range(1, SHARD_COUNT)
                .map(new Function<Integer, HashTagCounterShard>() {
                    @Override
                    public HashTagCounterShard apply(Integer shardId) throws Exception {
                        return HashTagCounterShard.builder()
                                .id(HashTagUtil.createShardId(hashTagKey, shardId))
                                .count(0)
                                .build();
                    }
                }).to(new Function<Observable<HashTagCounterShard>,
                        ImmutableList<HashTagCounterShard>>() {
                    @Override
                    public ImmutableList<HashTagCounterShard> apply(
                            Observable<HashTagCounterShard> o)
                            throws Exception {
                        return ImmutableList.copyOf(o.toList().blockingGet());
                    }
                });
    }

    public static ImmutableList<Key<HashTagCounterShard>> from(Collection<Key<HashTag>> tagKeys) {
        return Observable.fromIterable(tagKeys)
                .map(new Function<Key<HashTag>, List<Key<HashTagCounterShard>>>() {
                    @Override
                    public List<Key<HashTagCounterShard>> apply(final Key<HashTag> key)
                            throws Exception {
                        return Observable.range(1, SHARD_COUNT)
                                .map(new Function<Integer, Key<HashTagCounterShard>>() {
                                    @Override
                                    public Key<HashTagCounterShard> apply(Integer shardId)
                                            throws Exception {
                                        return Key.create(
                                                HashTagCounterShard.class,
                                                HashTagUtil.createShardId(key, shardId));
                                    }
                                })
                                .toList()
                                .blockingGet();
                    }
                })
                .toList()
                .map(new Function<List<List<Key<HashTagCounterShard>>>,
                        ImmutableList<Key<HashTagCounterShard>>>() {
                    @Override
                    public ImmutableList<Key<HashTagCounterShard>> apply(
                            List<List<Key<HashTagCounterShard>>> lists) throws Exception {
                        ImmutableList.Builder<Key<HashTagCounterShard>> builder =
                                ImmutableList.builder();

                        for (List<Key<HashTagCounterShard>> keys : lists) {
                            builder = builder.addAll(keys);
                        }

                        return builder.build();
                    }
                })
                .blockingGet();
    }
}
