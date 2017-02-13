package com.yoloo.backend.category.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.category.Category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CategorySorter {
    TRENDING("hot") {
        @Override
        public SortStrategy getSortStrategy(Query<Category> query) {
            return SortByTrending.newInstance(query);
        }
    },
    DEFAULT("default") {
        @Override
        protected SortStrategy getSortStrategy(Query<Category> query) {
            return SortByAll.newInstance(query);
        }
    };

    private String title;

    protected abstract SortStrategy getSortStrategy(Query<Category> query);

    public static Query<Category> sort(Query<Category> query, CategorySorter sorter) {
        return sorter.getSortStrategy(query).sort();
    }

    interface SortStrategy {
        Query<Category> sort();
    }
}
