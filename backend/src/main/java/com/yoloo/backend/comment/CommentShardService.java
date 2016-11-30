package com.yoloo.backend.comment;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.Key;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.comment.CommentCounterShard.SHARD_COUNT;

@AllArgsConstructor(staticName = "newInstance")
public class CommentShardService {

    public ImmutableList<CommentCounterShard> createShards(final Key<Comment> commentKey) {
        return Observable.range(1, SHARD_COUNT)
                .map(new Function<Integer, CommentCounterShard>() {
                    @Override
                    public CommentCounterShard apply(Integer shardId) throws Exception {
                        return createShard(commentKey, shardId);
                    }
                })
                .to(new Function<Observable<CommentCounterShard>,
                        ImmutableList<CommentCounterShard>>() {
                    @Override
                    public ImmutableList<CommentCounterShard> apply(Observable<CommentCounterShard> o)
                            throws Exception {
                        return ImmutableList.copyOf(o.toList().blockingGet());
                    }
                });
    }

    public CommentCounterShard createShard(Key<Comment> commentKey, int shardId) {
        return CommentCounterShard.builder()
                .id(CommentUtil.createShardId(commentKey, shardId))
                .votes(0)
                .build();
    }

    public ImmutableList<Key<CommentCounterShard>> getShardKeys(Key<Comment> key) {
        return getShardKeys(Collections.singletonList(key));
    }

    public ImmutableList<Key<CommentCounterShard>> getShardKeys(Collection<Key<Comment>> keys) {
        return Observable.fromIterable(keys)
                .map(new Function<Key<Comment>, List<Key<CommentCounterShard>>>() {
                    @Override
                    public List<Key<CommentCounterShard>> apply(final Key<Comment> key)
                            throws Exception {
                        return Observable.range(1, SHARD_COUNT)
                                .map(new Function<Integer, Key<CommentCounterShard>>() {
                                    @Override
                                    public Key<CommentCounterShard> apply(Integer shardId)
                                            throws Exception {
                                        return Key.create(
                                                CommentCounterShard.class,
                                                CommentUtil.createShardId(key, shardId));
                                    }
                                }).toList().blockingGet();
                    }
                })
                .toList()
                .to(new Function<Single<List<List<Key<CommentCounterShard>>>>,
                        ImmutableList<Key<CommentCounterShard>>>() {
                    @Override
                    public ImmutableList<Key<CommentCounterShard>> apply(
                            Single<List<List<Key<CommentCounterShard>>>> listSingle)
                            throws Exception {
                        ImmutableList.Builder<Key<CommentCounterShard>> builder =
                                ImmutableList.builder();

                        for (List<Key<CommentCounterShard>> keys : listSingle.blockingGet()) {
                            builder = builder.addAll(keys);
                        }

                        return builder.build();
                    }
                });
    }

    public Key<CommentCounterShard> getRandomShardKey(final Key<Comment> commentKey) {
        final int shardNum = new Random().nextInt(CommentCounterShard.SHARD_COUNT - 1 + 1) + 1;
        return Key.create(CommentCounterShard.class,
                CommentUtil.createShardId(commentKey, shardNum));
    }
}
