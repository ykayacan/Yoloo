package com.yoloo.backend.vote;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.util.Group;
import io.reactivex.Observable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class VoteService {

  private static final Logger LOG =
      Logger.getLogger(VoteService.class.getName());

  /**
   * Check post vote observable.
   *
   * @param post the post
   * @param accountKey the account key
   * @return the observable
   */
  public Observable<Post> checkPostVote(Post post, Key<Account> accountKey) {
    List<Post> posts = Collections.singletonList(post);

    return getVotesObservable(posts, accountKey)
        .flatMap(voteMap -> mergeVoteDirection(posts, accountKey, voteMap))
        .cast(Post.class);
  }

  /**
   * Check post vote observable.
   *
   * @param posts the postCount
   * @param accountKey the account key
   * @return the observable
   */
  public Observable<List<Post>> checkPostVote(List<Post> posts, Key<Account> accountKey) {
    return getVotesObservable(posts, accountKey)
        .flatMap(voteMap -> mergeVoteDirection(posts, accountKey, voteMap))
        .cast(Post.class)
        .toList()
        .toObservable();
  }

  /**
   * Check comment vote observable.
   *
   * @param comment the comment
   * @param accountKey the account key
   * @return the observable
   */
  public Observable<Comment> checkCommentVote(Comment comment, Key<Account> accountKey) {
    List<Comment> posts = Collections.singletonList(comment);

    return getVotesObservable(posts, accountKey)
        .flatMap(voteMap -> mergeVoteDirection(posts, accountKey, voteMap))
        .cast(Comment.class);
  }

  /**
   * Check comment vote observable.
   *
   * @param comments the comments
   * @param accountKey the account key
   * @return the observable
   */
  public Observable<List<Comment>> checkCommentVote(List<Comment> comments,
      Key<Account> accountKey) {
    return getVotesObservable(comments, accountKey)
        .flatMap(voteMap -> mergeVoteDirection(comments, accountKey, voteMap))
        .cast(Comment.class)
        .toList()
        .toObservable();
  }

  private Observable<Map<Key<Vote>, Vote>> getVotesObservable(List<? extends Votable> votables,
      Key<Account> accountKey) {
    return Observable.fromIterable(votables)
        .map(post -> Vote.createKey(post.getVotableKey(), accountKey))
        .toList()
        .flatMapObservable(keys -> Observable.just(ofy().load().keys(keys)));
  }

  private Observable<Votable> mergeVoteDirection(List<? extends Votable> votables,
      Key<Account> accountKey, Map<Key<Vote>, Vote> voteMap) {
    return Observable.fromIterable(votables)
        .map(votable ->
            Group.OfTwo.create(Vote.createKey(votable.getVotableKey(), accountKey), votable))
        .map(group -> voteMap.containsKey(group.first)
            ? group.second.setVoteDir(voteMap.get(group.first).getDir())
            : group.second);
  }
}
