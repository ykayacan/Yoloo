package com.yoloo.backend.shard;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ShardService2<E, S> {

    Observable<List<S>> createShards(Iterable<Key<E>> keys);

    Observable<List<S>> createShards(Key<E> entityKey);

    Single<S> createShard(Key<E> entityKey, int shardNum);

    Observable<List<Ref<S>>> createShardRefs(Iterable<Key<E>> keyIterable);

    Single<Key<S>> getRandomShardKey(Key<E> entityKey);
}
