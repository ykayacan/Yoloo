package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.yoloo.backend.util.StringUtil;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {

  public static Observable<Key<Tag>> extractGroupKeys(String groupIds) {
    return StringUtil.splitToSetObservable(groupIds, ",")
        .flatMap(Observable::fromIterable)
        .map(Key::<Tag>create);
  }

  public static Observable<Key<Tag>> extractTagKeys(String tagIds) {
    return Observable
        .fromIterable(StringUtil.splitToSet(tagIds, ","))
        .map(Key::<Tag>create);
  }
}
