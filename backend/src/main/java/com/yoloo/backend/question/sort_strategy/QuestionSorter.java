package com.yoloo.backend.question.sort_strategy;

import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.question.Question;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuestionSorter {
    NEWEST("newest") {
        @Override
        public SortStrategy getSortStrategy(Query<Question> query) {
            return SortByNewest.newInstance(query);
        }
    },
    HOT("hot") {
        @Override
        public SortStrategy getSortStrategy(Query<Question> query) {
            return SortByTrending.newInstance(query);
        }
    },
    UNANSWERED("unanswered") {
        @Override
        public SortStrategy getSortStrategy(Query<Question> query) {
            return SortByUnanswered.newInstance(query);
        }
    },
    BOUNTY("bounty") {
        @Override
        protected SortStrategy getSortStrategy(Query<Question> query) {
            return SortByBounty.newInstance(query);
        }
    };

    private String title;

    protected abstract SortStrategy getSortStrategy(Query<Question> query);

    public static Query<Question> sort(Query<Question> query, QuestionSorter sorter) {
        return sorter.getSortStrategy(query).sort();
    }

    interface SortStrategy {
        Query<Question> sort();
    }
}
