package com.yoloo.backend.hashtag;

import com.googlecode.objectify.Key;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashTagUtil {

    public static String createShardId(Key<HashTag> hashTagKey, int shardNum) {
        return hashTagKey.toWebSafeString() + ":" + String.valueOf(shardNum);
    }
}
