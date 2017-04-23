package com.yoloo.backend.post.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostSorter {
  NEWEST("newest") {
    @Override
    public SortStrategy getSortStrategy(Query<PostEntity> query) {
      return SortByNewest.create(query);
    }
  }, HOT("hot") {
    @Override
    public SortStrategy getSortStrategy(Query<PostEntity> query) {
      return SortByTrending.create(query);
    }
  }, UNANSWERED("unanswered") {
    @Override
    public SortStrategy getSortStrategy(Query<PostEntity> query) {
      return SortByUnanswered.create(query);
    }
  }, BOUNTY("bounty") {
    @Override
    protected SortStrategy getSortStrategy(Query<PostEntity> query) {
      return SortByBounty.create(query);
    }
  };

  private final String title;

  public static Query<PostEntity> sort(Query<PostEntity> query, PostSorter sorter) {
    return sorter.getSortStrategy(query).sort();
  }

  protected abstract SortStrategy getSortStrategy(Query<PostEntity> query);

  interface SortStrategy {
    Query<PostEntity> sort();
  }
}
