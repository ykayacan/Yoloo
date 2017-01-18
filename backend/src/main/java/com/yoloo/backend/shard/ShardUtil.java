package com.yoloo.backend.shard;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import io.reactivex.Observable;

public final class ShardUtil {

  public static <E> String generateShardId(Key<E> entityKey, int shardNum) {
    return entityKey.toWebSafeString() + ":" + shardNum;
  }

  public static <E> Observable<Ref<E>> createRefs(Iterable<E> entities) {
    return Observable
        .fromIterable(entities)
        .flatMap(e -> Observable.just(Ref.create(e)));
  }
}
