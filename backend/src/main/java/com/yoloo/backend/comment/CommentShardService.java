package com.yoloo.backend.comment;

import com.googlecode.objectify.Key;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;

import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "newInstance")
public class CommentShardService implements ShardService<Comment, CommentCounterShard> {

    @Override
    public List<Key<CommentCounterShard>> createShardKeys(Iterable<Key<Comment>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<Comment>,
                        Iterable<Key<CommentCounterShard>>>() {
                    @Override
                    public Iterable<Key<CommentCounterShard>> apply(Key<Comment> key)
                            throws Exception {
                        return createShardKeys(key);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<Key<CommentCounterShard>> createShardKeys(final Key<Comment> entityKey) {
        return Observable
                .range(1, CommentCounterShard.SHARD_COUNT)
                .map(new Function<Integer, Key<CommentCounterShard>>() {
                    @Override
                    public Key<CommentCounterShard> apply(Integer id)
                            throws Exception {
                        return createShardKey(entityKey, id);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<Key<CommentCounterShard>> getShardKeys(Iterable<Comment> entities) {
        return Observable
                .fromIterable(entities)
                .concatMapIterable(new Function<Comment, Iterable<Key<CommentCounterShard>>>() {
                    @Override
                    public Iterable<Key<CommentCounterShard>> apply(Comment comment) throws Exception {
                        return getShardKeys(comment);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<Key<CommentCounterShard>> getShardKeys(Comment entity) {
        return entity.getShardKeys();
    }

    @Override
    public Key<CommentCounterShard> createShardKey(Key<Comment> entityKey, int shardNum) {
        return Key.create(CommentCounterShard.class,
                ShardUtil.generateShardId(entityKey, shardNum));
    }

    @Override
    public List<CommentCounterShard> createShards(Iterable<Key<Comment>> keys) {
        return Observable
                .fromIterable(keys)
                .concatMapIterable(new Function<Key<Comment>, Iterable<CommentCounterShard>>() {
                    @Override
                    public Iterable<CommentCounterShard> apply(Key<Comment> key) throws Exception {
                        return createShards(key);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public List<CommentCounterShard> createShards(final Key<Comment> entityKey) {
        return Observable
                .range(1, CommentCounterShard.SHARD_COUNT)
                .map(new Function<Integer, CommentCounterShard>() {
                    @Override
                    public CommentCounterShard apply(Integer shardId) throws Exception {
                        return createShard(entityKey, shardId);
                    }
                })
                .toList()
                .blockingGet();
    }

    @Override
    public CommentCounterShard createShard(Key<Comment> entityKey, int shardNum) {
        return CommentCounterShard.builder()
                .id(ShardUtil.generateShardId(entityKey, shardNum))
                .votes(0)
                .build();
    }

    @Override
    public Key<CommentCounterShard> getRandomShardKey(Key<Comment> entityKey) {
        final int shardNum = new Random().nextInt(CommentCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return createShardKey(entityKey, shardNum);
    }
}
