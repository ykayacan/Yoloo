package com.yoloo.backend.comment;

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
public final class CommentUtil {

  /**
   * Merge comment counts observable.
   *
   * @param comment the comment
   * @return the observable
   */
  public static Observable<Comment> mergeCommentCounts(Comment comment) {
    return mergeCommentCounts(Lists.newArrayList(comment));
  }

  /**
   * Merge comment counts observable.
   *
   * @param comments the comments
   * @return the observable
   */
  public static Observable<Comment> mergeCommentCounts(Collection<Comment> comments) {
    if (comments.isEmpty()) {
      return Observable.empty();
    }

    return Observable.fromIterable(comments).flatMap(CommentUtil::mergeShards);
  }

  /**
   * Merge vote counts observable.
   *
   * @param comment the comment
   * @param parentKey the parent key
   * @return the observable
   */
  public static Observable<Comment> mergeVoteDirection(Comment comment, Key<Account> parentKey) {
    return mergeVoteDirection(Lists.newArrayList(comment), parentKey);
  }

  /**
   * Merge vote counts observable.
   *
   * @param comments the comments
   * @param parentKey the parent key
   * @return the observable
   */
  public static Observable<Comment> mergeVoteDirection(final List<Comment> comments,
      final Key<Account> parentKey) {

    if (comments.isEmpty()) {
      return Observable.empty();
    }

    final VoteHelper helper = VoteHelper.create();

    Pair<Comment, Comment> pair = helper.sort(comments, true);

    QueryResultIterable<Vote> votes = helper.getVotes(pair.first, pair.second, parentKey);

    return Observable.fromIterable(comments).flatMap(comment -> mergeVoteDir(comment, votes));
  }

  private static Observable<Comment> mergeVoteDir(Comment comment,
      QueryResultIterable<Vote> votes) {

    for (Vote vote : votes) {
      if (comment.getKey().equals(vote.getVotableKey())) {
        comment = comment.withDir(vote.getDir());
      }
    }

    return Observable.just(comment);
  }

  private static Observable<Comment> mergeShards(Comment comment) {
    return Observable.fromIterable(comment.getShards())
        .map(CommentCounterShard::getVotes)
        .reduce((val1, val2) -> val1 + val2)
        .map(comment::withVotes)
        .toObservable();
  }

  public static String trimmedContent(Comment comment, int limit) {
    String content = comment.getContent();

    if (content.length() > limit) {
      return content.substring(0, limit);
    }

    return content;
  }
}
