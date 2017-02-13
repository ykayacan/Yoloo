package com.yoloo.backend.post.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.post.Post;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
final class SortByTrending implements PostSorter.SortStrategy {

  private final Query<Post> query;

  @Override
  public Query<Post> sort() {
    return query.order("-" + Post.FIELD_RANK);
  }
}
