package com.yoloo.backend.group.sorter;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.group.TravelerGroupEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
final class SortByAll implements GroupSorter.SortStrategy {

    private final Query<TravelerGroupEntity> query;

    @Override
    public Query<TravelerGroupEntity> sort() {
        return query;
    }
}
