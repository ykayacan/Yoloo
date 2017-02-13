package com.yoloo.backend.vote;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.post.Post;
import io.reactivex.Observable;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class VoteService {

  /**
   * Check post vote observable.
   *
   * @param post the post
   * @param accountKey the account key
   * @param sorted the sorted
   * @return the observable
   */
  public Observable<Post> checkPostVote(Post post, Key<Account> accountKey, boolean sorted) {

    QueryResultIterable<Vote> votes =
        VoteUtil.mergeVotes(Collections.singletonList(post), accountKey, sorted);

    return Observable.just(post).map(post1 -> updatePostWithVote(votes, post1));
  }

  /**
   * Check comment vote observable.
   *
   * @param comment the comment
   * @param accountKey the account key
   * @param sorted the sorted
   * @return the observable
   */
  public Observable<Comment> checkCommentVote(Comment comment, Key<Account> accountKey,
      boolean sorted) {

    QueryResultIterable<Vote> votes =
        VoteUtil.mergeVotes(Collections.singletonList(comment), accountKey, sorted);

    return Observable.just(comment).map(post1 -> updateCommentWithVote(votes, post1));
  }

  /**
   * Check post vote observable.
   *
   * @param posts the posts
   * @param accountKey the account key
   * @param sorted the sorted
   * @return the observable
   */
  public Observable<List<Post>> checkPostVote(List<Post> posts, Key<Account> accountKey,
      boolean sorted) {

    QueryResultIterable<Vote> votes = VoteUtil.mergeVotes(posts, accountKey, sorted);

    return Observable.fromIterable(posts)
        .map(post -> updatePostWithVote(votes, post))
        .toList()
        .toObservable();
  }

  /**
   * Check comment vote observable.
   *
   * @param comments the comments
   * @param accountKey the account key
   * @param sorted the sorted
   * @return the observable
   */
  public Observable<List<Comment>> checkCommentVote(List<Comment> comments, Key<Account> accountKey,
      boolean sorted) {

    QueryResultIterable<Vote> votes = VoteUtil.mergeVotes(comments, accountKey, sorted);

    return Observable.fromIterable(comments)
        .map(post -> updateCommentWithVote(votes, post))
        .toList()
        .toObservable();
  }

  private Post updatePostWithVote(QueryResultIterable<Vote> votes, Post post) {
    for (Vote vote : votes) {
      post = post.getKey().equals(vote.getVotableKey()) ? post.withDir(vote.getDir()) : post;
    }

    return post;
  }

  private Comment updateCommentWithVote(QueryResultIterable<Vote> votes, Comment comment) {
    for (Vote vote : votes) {
      comment = comment.getKey().equals(vote.getVotableKey())
          ? comment.withDir(vote.getDir())
          : comment;
    }

    return comment;
  }
}
