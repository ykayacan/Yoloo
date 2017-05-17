package com.yoloo.backend.vote;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.shard.Shardable;
import io.reactivex.Observable;
import ix.Ix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;
import static com.yoloo.backend.endpointsvalidator.Guard.checkNotFound;

@Log
@AllArgsConstructor(staticName = "create")
public final class VoteController extends Controller {

  private PostShardService postShardService;

  private CommentShardService commentShardService;

  private VoteService voteService;

  public Comment voteComment(@Nonnull String commentId, int dir, @Nonnull User user) {
    vote(commentId, dir, user);

    return Observable
        .fromCallable(() -> {
          Comment comment = ofy().load()
              .group(Comment.ShardGroup.class)
              .key(Key.<Comment>create(commentId))
              .now();

          return checkNotFound(comment, "Comment does not exists!");
        })
        .flatMap(commentShardService::mergeShards)
        .flatMap(comment -> voteService.checkCommentVote(comment, Key.create(user.getUserId())))
        .blockingSingle();
  }

  public PostEntity votePost(@Nonnull String postId, int dir, @Nonnull User user) {
    vote(postId, dir, user);

    return Observable
        .fromCallable(() -> {
          PostEntity post = ofy().load()
              .group(PostEntity.ShardGroup.class)
              .key(Key.<PostEntity>create(postId))
              .now();

          return checkNotFound(post, "Post does not exists!");
        })
        .flatMap(postShardService::mergeShards)
        .flatMap(comment -> voteService.checkPostVote(comment, Key.create(user.getUserId())))
        .blockingSingle();
  }

  private void vote(String votableId, int dir, User user) {
    Vote.Direction direction = Vote.parse(dir);

    final Key<Votable> votableKey = Key.create(votableId);
    final Key<Account> accountKey = Key.create(user.getUserId());

    Shardable.Shard shard = getShard(votableKey);
    Vote dbVote = getVote(votableKey, accountKey);

    if (dbVote == null) {
      Vote vote = Vote
          .builder()
          .id(votableKey.toWebSafeString())
          .parent(accountKey)
          .votableKey(votableKey)
          .dir(dir)
          .build();

      switch (direction) {
        case UP:
          shard.increaseVotesBy(1L);
          break;
        case DOWN:
          shard.decreaseVotesBy(1L);
          break;
      }

      ofy().transact(() -> ofy().save().entities(vote, shard).now());
    } else {
      switch (Vote.parse(dbVote.getDir())) {
        case DEFAULT:
          if (direction == Vote.Direction.UP) {
            shard.increaseVotesBy(1L);
          } else if (direction == Vote.Direction.DOWN) {
            shard.decreaseVotesBy(1L);
          }
          break;
        case UP:
          if (direction == Vote.Direction.DEFAULT) {
            shard.decreaseVotesBy(1L);
          } else if (direction == Vote.Direction.DOWN) {
            shard.decreaseVotesBy(2L);
          }
          break;
        case DOWN:
          if (direction == Vote.Direction.DEFAULT) {
            shard.increaseVotesBy(1L);
          } else if (direction == Vote.Direction.UP) {
            shard.increaseVotesBy(2L);
          }
          break;
      }

      ofy().transact(() -> ofy().save().entities(dbVote.withDir(dir), shard).now());
    }
  }

  public CollectionResponse<Account> listVoters(@Nonnull String postId, Optional<Integer> limit,
      Optional<String> cursor) {
    Query<Vote> query = ofy().load().type(Vote.class).filter(Vote.FIELD_VOTABLE_KEY + "=", postId);
    query = cursor.isPresent() ? query.startAt(Cursor.fromWebSafeString(cursor.get())) : query;
    query = query.limit(limit.or(20));

    List<Vote> votes = new ArrayList<>(20);

    QueryResultIterator<Vote> qi = query.iterator();

    while (qi.hasNext()) {
      votes.add(qi.next());
    }

    List<Key<Account>> voterKeys = Ix.from(votes).map(Vote::getParent).toList();
    Collection<Account> voters = ofy().load().keys(voterKeys).values();

    return CollectionResponse.<Account>builder()
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .setItems(voters)
        .build();
  }

  private Vote getVote(Key<? extends Votable> votableKey, Key<Account> accountKey) {
    return ofy().load().key(Vote.createKey(votableKey, accountKey)).now();
  }

  private Shardable.Shard getShard(Key<? extends Votable> votableKey) {
    Key<?> shardKey;
    if (votableKey.getKind().equals(Key.getKind(PostEntity.class))) {
      shardKey = postShardService.getRandomShardKey(Key.create(votableKey.getRaw()));
    } else {
      shardKey = commentShardService.getRandomShardKey(Key.create(votableKey.getRaw()));
    }
    return (Shardable.Shard) ofy().load().key(shardKey).now();
  }
}
