package com.yoloo.backend.post.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
final class SortByBounty implements PostSorter.SortStrategy {

  private final Query<PostEntity> query;

  @Override
  public Query<PostEntity> sort() {
    return query.filter(PostEntity.FIELD_BOUNTY + " >", 0)
        .order("-" + PostEntity.FIELD_BOUNTY)
        .order("-" + PostEntity.FIELD_CREATED);
  }
}
