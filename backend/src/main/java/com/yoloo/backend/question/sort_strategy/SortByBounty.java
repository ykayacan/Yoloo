package com.yoloo.backend.question.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.question.Question;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
final class SortByBounty implements QuestionSorter.SortStrategy {

    private final Query<Question> query;

    @Override
    public Query<Question> sort() {
        return query.filter(Question.FIELD_BOUNTY + " >", 0)
                .order("-" + Question.FIELD_BOUNTY)
                .order("-" + Question.FIELD_CREATED);
    }
}
