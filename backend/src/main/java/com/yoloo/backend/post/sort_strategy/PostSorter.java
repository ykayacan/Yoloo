package com.yoloo.backend.post.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.post.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostSorter {
  NEWEST("newest") {
    @Override
    public SortStrategy getSortStrategy(Query<Post> query) {
      return SortByNewest.create(query);
    }
  }, HOT("hot") {
    @Override
    public SortStrategy getSortStrategy(Query<Post> query) {
      return SortByTrending.create(query);
    }
  }, UNANSWERED("unanswered") {
    @Override
    public SortStrategy getSortStrategy(Query<Post> query) {
      return SortByUnanswered.create(query);
    }
  }, BOUNTY("bounty") {
    @Override
    protected SortStrategy getSortStrategy(Query<Post> query) {
      return SortByBounty.create(query);
    }
  };

  private final String title;

  public static Query<Post> sort(Query<Post> query, PostSorter sorter) {
    return sorter.getSortStrategy(query).sort();
  }

  protected abstract SortStrategy getSortStrategy(Query<Post> query);

  interface SortStrategy {
    Query<Post> sort();
  }
}
