package com.yoloo.backend.category;

import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CategoryUtil {

  static Observable<Category> mergeShards(Category category) {
    return Observable.fromIterable(category.getShards())
        .cast(CategoryShard.class)
        .reduce((s1, s2) -> s1.addPost(s2.getPosts()))
        .map(shard -> category.withQuestions(shard.getPosts()))
        .toObservable();
  }
}
