package com.yoloo.backend.vote;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.util.Group;
import io.reactivex.Observable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
@AllArgsConstructor(staticName = "create")
public class VoteService {

  /**
   * Check post vote observable.
   *
   * @param postEntity the post
   * @param accountKey the account key
   * @return the observable
   */
  public Observable<PostEntity> checkPostVote(PostEntity postEntity, Key<Account> accountKey) {
    List<PostEntity> postEntities = Collections.singletonList(postEntity);

    return getVotesObservable(postEntities, accountKey)
        .flatMap(voteMap -> mergeVoteDirection(postEntities, accountKey, voteMap))
        .cast(PostEntity.class);
  }

  /**
   * Check post vote observable.
   *
   * @param posts the postCount
   * @param accountKey the account key
   * @return the observable
   */
  public Observable<List<PostEntity>> checkPostVote(List<PostEntity> posts,
      Key<Account> accountKey) {
    return getVotesObservable(posts, accountKey)
        .flatMap(voteMap -> mergeVoteDirection(posts, accountKey, voteMap))
        .cast(PostEntity.class)
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
    return Observable
        .fromIterable(votables)
        .map(votable -> Vote.createKey(votable.getVotableKey(), accountKey))
        .toList()
        .flatMapObservable(keys -> Observable.just(ofy().load().keys(keys)));
  }

  private Observable<Votable> mergeVoteDirection(List<? extends Votable> votables,
      Key<Account> accountKey, Map<Key<Vote>, Vote> voteMap) {
    return Observable
        .fromIterable(votables)
        .map(votable -> Group.OfTwo.create(Vote.createKey(votable.getVotableKey(), accountKey),
            votable))
        .map(group -> voteMap.containsKey(group.first)
            ? group.second.setVoteDir(Vote.parse(voteMap.get(group.first).getDir()))
            : group.second.setVoteDir(Vote.Direction.DEFAULT));
  }
}
