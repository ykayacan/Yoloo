package com.yoloo.backend.post.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.post.Post;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
final class SortByBounty implements PostSorter.SortStrategy {

  private final Query<Post> query;

  @Override
  public Query<Post> sort() {
    return query.filter(Post.FIELD_BOUNTY + " >", 0)
        .order("-" + Post.FIELD_BOUNTY)
        .order("-" + Post.FIELD_CREATED);
  }
}
