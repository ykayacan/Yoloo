package com.yoloo.backend.category.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.category.Category;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
final class SortByAll implements CategorySorter.SortStrategy {

    private final Query<Category> query;

    @Override
    public Query<Category> sort() {
        return query;
    }
}
