package com.yoloo.backend.hashtag;

import com.googlecode.objectify.Key;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;

import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "newInstance")
public class HashTagShardService implements ShardService<HashTag, HashTagCounterShard> {

    @Override
    public List<Key<HashTagCounterShard>> createShardKeys(Iterable<Key<HashTag>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<HashTag>,
                        Iterable<Key<HashTagCounterShard>>>() {
                    @Override
                    public Iterable<Key<HashTagCounterShard>> apply(Key<HashTag> key)
                            throws Exception {
                        return createShardKeys(key);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<Key<HashTagCounterShard>> createShardKeys(final Key<HashTag> entityKey) {
        return Observable
                .range(1, ShardConfig.HASHTAG_SHARD_COUNTER)
                .map(new Function<Integer, Key<HashTagCounterShard>>() {
                    @Override
                    public Key<HashTagCounterShard> apply(Integer id)
                            throws Exception {
                        return createShardKey(entityKey, id);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<Key<HashTagCounterShard>> getShardKeys(Iterable<HashTag> entities) {
        return Observable
                .fromIterable(entities)
                .concatMapIterable(new Function<HashTag, Iterable<Key<HashTagCounterShard>>>() {
                    @Override
                    public Iterable<Key<HashTagCounterShard>> apply(HashTag hashTag) throws Exception {
                        return getShardKeys(hashTag);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<Key<HashTagCounterShard>> getShardKeys(HashTag entity) {
        return entity.getShardKeys();
    }

    @Override
    public Key<HashTagCounterShard> createShardKey(Key<HashTag> entityKey, int shardNum) {
        return Key.create(HashTagCounterShard.class, ShardUtil.generateShardId(entityKey, shardNum));
    }

    @Override
    public List<HashTagCounterShard> createShards(Iterable<Key<HashTag>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<HashTag>, Iterable<HashTagCounterShard>>() {
                    @Override
                    public Iterable<HashTagCounterShard> apply(Key<HashTag> key) throws Exception {
                        return createShards(key);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<HashTagCounterShard> createShards(final Key<HashTag> entityKey) {
        return Observable
                .range(1, ShardConfig.HASHTAG_SHARD_COUNTER)
                .map(new Function<Integer, HashTagCounterShard>() {
                    @Override
                    public HashTagCounterShard apply(Integer shardId) throws Exception {
                        return createShard(entityKey, shardId);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public HashTagCounterShard createShard(Key<HashTag> entityKey, int shardNum) {
        return HashTagCounterShard.builder()
                .id(ShardUtil.generateShardId(entityKey, shardNum))
                .questions(0)
                .build();
    }

    @Override
    public Key<HashTagCounterShard> getRandomShardKey(Key<HashTag> entityKey) {
        final int shardNum = new Random().nextInt(HashTagCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return createShardKey(entityKey, shardNum);
    }
}
