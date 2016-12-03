package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;

import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class TagShardService implements ShardService<Tag, TagCounterShard> {

    @Override
    public List<Key<TagCounterShard>> createShardKeys(Iterable<Key<Tag>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<Tag>,
                        Iterable<Key<TagCounterShard>>>() {
                    @Override
                    public Iterable<Key<TagCounterShard>> apply(Key<Tag> key)
                            throws Exception {
                        return createShardKeys(key);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<Key<TagCounterShard>> createShardKeys(final Key<Tag> entityKey) {
        return Observable
                .range(1, ShardConfig.HASHTAG_SHARD_COUNTER)
                .map(new Function<Integer, Key<TagCounterShard>>() {
                    @Override
                    public Key<TagCounterShard> apply(Integer id)
                            throws Exception {
                        return createShardKey(entityKey, id);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<Key<TagCounterShard>> getShardKeys(Iterable<Tag> entities) {
        return Observable
                .fromIterable(entities)
                .concatMapIterable(new Function<Tag, Iterable<Key<TagCounterShard>>>() {
                    @Override
                    public Iterable<Key<TagCounterShard>> apply(Tag tag) throws Exception {
                        return getShardKeys(tag);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<Key<TagCounterShard>> getShardKeys(Tag entity) {
        return null;
    }

    @Override
    public Key<TagCounterShard> createShardKey(Key<Tag> entityKey, int shardNum) {
        return Key.create(TagCounterShard.class, ShardUtil.generateShardId(entityKey, shardNum));
    }

    @Override
    public List<TagCounterShard> createShards(Iterable<Key<Tag>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<Tag>, Iterable<TagCounterShard>>() {
                    @Override
                    public Iterable<TagCounterShard> apply(Key<Tag> key) throws Exception {
                        return createShards(key);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public List<TagCounterShard> createShards(final Key<Tag> entityKey) {
        return Observable
                .range(1, ShardConfig.HASHTAG_SHARD_COUNTER)
                .map(new Function<Integer, TagCounterShard>() {
                    @Override
                    public TagCounterShard apply(Integer shardId) throws Exception {
                        return createShard(entityKey, shardId);
                    }
                })
                .toList()
                .cache()
                .blockingGet();
    }

    @Override
    public TagCounterShard createShard(Key<Tag> entityKey, int shardNum) {
        return TagCounterShard.builder()
                .id(ShardUtil.generateShardId(entityKey, shardNum))
                .questions(0)
                .build();
    }

    @Override
    public Key<TagCounterShard> getRandomShardKey(Key<Tag> entityKey) {
        final int shardNum = new Random().nextInt(TagCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return createShardKey(entityKey, shardNum);
    }

    public Single<List<Ref<TagCounterShard>>> createShardRefs(final Key<Tag> entityKey) {
        return Observable
                .range(1, Tag.SHARD_COUNT)
                .map(new Function<Integer, Ref<TagCounterShard>>() {
                    @Override
                    public Ref<TagCounterShard> apply(Integer id)
                            throws Exception {
                        return Ref.create(createShardKey(entityKey, id));
                    }
                })
                .cache()
                .toList();
    }
}
