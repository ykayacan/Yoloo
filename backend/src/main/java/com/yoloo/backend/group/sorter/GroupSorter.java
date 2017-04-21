package com.yoloo.backend.group.sorter;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.group.TravelerGroupEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupSorter {
  TRENDING("trending") {
    @Override
    public SortStrategy getSortStrategy(Query<TravelerGroupEntity> query) {
      return SortByTrending.newInstance(query);
    }
  }, DEFAULT("default") {
    @Override
    protected SortStrategy getSortStrategy(Query<TravelerGroupEntity> query) {
      return SortByAll.newInstance(query);
    }
  };

  private String title;

  public static Query<TravelerGroupEntity> sort(Query<TravelerGroupEntity> query, GroupSorter sorter) {
    return sorter.getSortStrategy(query).sort();
  }

  protected abstract SortStrategy getSortStrategy(Query<TravelerGroupEntity> query);

  interface SortStrategy {
    Query<TravelerGroupEntity> sort();
  }
}
