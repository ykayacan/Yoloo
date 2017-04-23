package com.yoloo.backend.comment;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.vote.Vote;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class CommentService {

  private CommentShardService commentShardService;

  public CommentEntity createComment(Account account, Key<PostEntity> postKey, String content) {
    final Key<Comment> commentKey = factory().allocateId(account.getKey(), Comment.class);

    Map<Ref<CommentShard>, CommentShard> shardMap =
        commentShardService.createShardMapWithRef(commentKey);

    // Create new comment.
    Comment comment = Comment.builder()
        .id(commentKey.getId())
        .parent(account.getKey())
        .postKey(postKey)
        .shardRefs(Lists.newArrayList(shardMap.keySet()))
        .content(content)
        .username(account.getUsername())
        .avatarUrl(account.getAvatarUrl())
        .dir(Vote.Direction.DEFAULT)
        .accepted(false)
        .voteCount(0L)
        .created(DateTime.now())
        .build();

    return CommentEntity.builder().comment(comment).shards(shardMap).build();
  }

  public Comment update(Comment comment, Optional<String> content) {
    if (content.isPresent()) {
      comment = comment.withContent(content.get());
    }

    return comment;
  }

  public List<Key<Vote>> getVoteKeys(Collection<Key<Comment>> keys) {
    Query<Vote> query = ofy().load().type(Vote.class);

    for (Key<Comment> key : keys) {
      query = query.filter(Vote.FIELD_VOTABLE_KEY + " =", key);
    }

    return query.keys().list();
  }

  public Observable<QueryResultIterable<Key<Vote>>> getVoteKeysObservable(
      Collection<Key<Comment>> keys) {
    return Observable.fromCallable(() -> {
      Query<Vote> query = ofy().load().type(Vote.class);

      for (Key<Comment> key : keys) {
        query = query.filter(Vote.FIELD_VOTABLE_KEY + " =", key);
      }

      return query.keys().iterable();
    });
  }
}
