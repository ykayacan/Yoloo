package com.yoloo.backend.question.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.question.Question;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
final class SortByUnanswered implements QuestionSorter.SortStrategy {

    private final Query<Question> query;

    @Override
    public Query<Question> sort() {
        return query.filter(Question.FIELD_FIRST_COMMENT + " =", false);
    }
}
