package com.yoloo.backend.shard;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Shardable<S extends Shardable.Shard, E> {

  Map<Ref<S>, S> createShardMapWithRef(Iterable<Key<E>> keys);

  Map<Key<S>, S> createShardMapWithKey(Iterable<Key<E>> keys);

  Map<Ref<S>, S> createShardMapWithRef(Key<E> key);

  Map<Key<S>, S> createShardMapWithKey(Key<E> key);

  Key<S> getRandomShardKey(Key<E> entityKey);

  Observable<List<E>> mergeShards(Collection<? extends E> entities);

  Observable<E> mergeShards(E entity);

  interface Shard {
    void increaseVotesBy(long value);

    void decreaseVotesBy(long value);
  }
}
