package com.yoloo.backend.topic.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.topic.Topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CategorySorter {
    TRENDING("hot") {
        @Override
        public SortStrategy getSortStrategy(Query<Topic> query) {
            return SortByTrending.newInstance(query);
        }
    },
    DEFAULT("default") {
        @Override
        protected SortStrategy getSortStrategy(Query<Topic> query) {
            return SortByAll.newInstance(query);
        }
    };

    private String title;

    protected abstract SortStrategy getSortStrategy(Query<Topic> query);

    public static Query<Topic> sort(Query<Topic> query, CategorySorter sorter) {
        return sorter.getSortStrategy(query).sort();
    }

    interface SortStrategy {
        Query<Topic> sort();
    }
}
