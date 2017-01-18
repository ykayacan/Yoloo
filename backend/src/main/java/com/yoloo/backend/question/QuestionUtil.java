package com.yoloo.backend.question;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.vote.Vote;
import com.yoloo.backend.vote.VoteHelper;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuestionUtil {

  /**
   * Merge comment counts observable.
   *
   * @param question the comment
   * @return the observable
   */
  public static Observable<Question> mergeCounts(Question question) {
    return mergeCounts(Lists.newArrayList(question));
  }

  /**
   * Merge comment counts observable.
   *
   * @param questions the comments
   * @return the observable
   */
  public static Observable<Question> mergeCounts(Collection<Question> questions) {
    if (questions.isEmpty()) return Observable.empty();

    return Observable.fromIterable(questions).flatMap(QuestionUtil::mergeShards);
  }

  /**
   * Merge vote counts observable.
   *
   * @param question the comment
   * @param parentKey the parent key
   * @return the observable
   */
  public static Observable<Question> mergeVoteDirection(Question question,
      Key<Account> parentKey, boolean sorted) {
    return mergeVoteDirection(Lists.newArrayList(question), parentKey, sorted);
  }

  /**
   * Merge vote counts observable.
   *
   * @param questions the comments
   * @param parentKey the parent key
   * @return the observable
   */
  public static Observable<Question> mergeVoteDirection(List<Question> questions,
      Key<Account> parentKey, boolean sorted) {

    if (questions.isEmpty()) {
      return Observable.empty();
    }

    final VoteHelper helper = VoteHelper.create();

    Pair<Question, Question> pair = helper.sort(questions, sorted);

    QueryResultIterable<Vote> votes = helper.getVotes(pair.first, pair.second, parentKey);

    return Observable
        .fromIterable(questions)
        .flatMap(question -> {
          for (Vote vote : votes) {
            if (question.getKey().equals(vote.getVotableKey())) {
              question = question.withDir(vote.getDir());
            }
          }

          return Observable.just(question);
        });
  }

  private static Observable<Question> mergeShards(Question question) {
    return Observable.fromIterable(question.getShards())
        .cast(QuestionCounterShard.class)
        .reduce(QuestionUtil::reduceCounters)
        .map(s -> mapToQuestion(question, s))
        .toObservable();
  }

  private static QuestionCounterShard reduceCounters(QuestionCounterShard s1,
      QuestionCounterShard s2) {
    return s1.addValues(s2.getComments(), s2.getVotes(), s2.getReports());
  }

  private static Question mapToQuestion(Question question, QuestionCounterShard s) {
    return question
        .withComments(s.getComments())
        .withVotes(s.getVotes())
        .withReports(s.getReports());
  }
}
