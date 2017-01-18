package com.yoloo.backend.tag;

import com.googlecode.objectify.Key;
import com.yoloo.backend.util.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagUtil {

  public static Single<List<Key<TagGroup>>> extractGroupKeys(String groupIds) {
    Set<String> groupIdSet = StringUtil.splitToSet(groupIds, ",");
    return Observable
        .fromIterable(groupIdSet)
        .map(Key::<TagGroup>create)
        .toList(groupIdSet.size());
  }

  public static Observable<Key<Tag>> extractTagKeys(String tagIds) {
    return Observable
        .fromIterable(StringUtil.splitToSet(tagIds, ","))
        .map(Key::<Tag>create);
  }
}
