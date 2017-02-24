package com.yoloo.backend.shard;

import com.googlecode.objectify.Key;

public final class ShardUtil {

  public static <E> String generateShardId(Key<E> entityKey, int shardNum) {
    return entityKey.toWebSafeString() + ":" + shardNum;
  }
}
