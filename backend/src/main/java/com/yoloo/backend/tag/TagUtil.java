package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.yoloo.backend.util.StringUtil;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {

    public static Single<List<Key<TagGroup>>> extractGroupKeys(String groupIds) {
        return Observable
                .fromIterable(StringUtil.splitToSet(groupIds, ","))
                .map(new Function<String, Key<TagGroup>>() {
                    @Override
                    public Key<TagGroup> apply(String s) throws Exception {
                        return Key.create(s);
                    }
                })
                .toList()
                .cache();
    }
}
