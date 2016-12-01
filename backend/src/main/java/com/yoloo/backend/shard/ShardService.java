package com.yoloo.backend.shard;

import com.googlecode.objectify.Key;

import java.util.List;

public interface ShardService<E, S> {

    List<Key<S>> createShardKeys(Iterable<Key<E>> keyIterable);

    List<Key<S>> createShardKeys(Key<E> entityKey);

    List<Key<S>> getShardKeys(Iterable<E> entities);

    List<Key<S>> getShardKeys(E entity);

    Key<S> createShardKey(Key<E> entityKey, int shardNum);

    List<S> createShards(Iterable<Key<E>> keyIterable);

    List<S> createShards(Key<E> entityKey);

    S createShard(Key<E> entityKey, int shardNum);

    Key<S> getRandomShardKey(Key<E> entityKey);
}