package com.yoloo.backend.util;

import com.google.common.base.Splitter;
import com.googlecode.objectify.Key;
import io.reactivex.Observable;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyUtil {

  public static <T> Observable<List<Key<T>>> extractKeysFromIds(String ids, String delimiter) {
    return Observable.fromIterable(
        Splitter.on(delimiter).trimResults().omitEmptyStrings().split(ids))
        .map(Key::<T>create)
        .toList()
        .toObservable();
  }
}
