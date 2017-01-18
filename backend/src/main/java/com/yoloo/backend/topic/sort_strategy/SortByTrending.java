package com.yoloo.backend.topic.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.topic.Topic;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
final class SortByTrending implements CategorySorter.SortStrategy {

    private final Query<Topic> query;

    @Override
    public Query<Topic> sort() {
        return query.order("-rank");
    }
}
