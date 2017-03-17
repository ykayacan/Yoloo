package com.yoloo.backend.util;

import com.google.common.base.Splitter;
import com.googlecode.objectify.Key;

import java.util.List;

import ix.Ix;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class KeyUtil {

  public static <T> List<Key<T>> extractKeysFromIds(String ids, String delimiter) {
    return Ix.from(Splitter.on(delimiter).trimResults().omitEmptyStrings().split(ids))
        .map(Key::<T>create)
        .toList();
  }
}
