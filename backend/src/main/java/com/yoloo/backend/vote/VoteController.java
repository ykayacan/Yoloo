package com.yoloo.backend.vote;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.post.PostShardService;
import com.yoloo.backend.shard.Shardable;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public final class VoteController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(VoteController.class.getName());

  private PostShardService postShardService;

  private CommentShardService commentShardService;

  public void vote(String votableId, Vote.Direction dir, User user) {
    final Key<Votable> votableKey = Key.create(votableId);
    final Key<Account> accountKey = Key.create(user.getUserId());

    Shardable.Shard shard = getShard(votableKey);
    Vote dbVote = getVote(votableKey, accountKey);

    if (dbVote == null) {
      Vote vote = Vote.builder()
          .id(votableKey.toWebSafeString())
          .parent(accountKey)
          .votableKey(votableKey)
          .dir(dir)
          .build();

      switch (dir) {
        case UP:
          shard.increaseVotesBy(1L);
          break;
        case DOWN:
          shard.decreaseVotesBy(1L);
          break;
      }

      ofy().transact(() -> ofy().save().entities(vote, shard).now());
    } else {
      switch (dbVote.getDir()) {
        case DEFAULT:
          if (dir == Vote.Direction.UP) {
            shard.increaseVotesBy(1L);
          } else if (dir == Vote.Direction.DOWN) {
            shard.decreaseVotesBy(1L);
          }
          break;
        case UP:
          if (dir == Vote.Direction.DEFAULT) {
            shard.decreaseVotesBy(1L);
          } else if (dir == Vote.Direction.DOWN) {
            shard.decreaseVotesBy(2L);
          }
          break;
        case DOWN:
          if (dir == Vote.Direction.DEFAULT) {
            shard.increaseVotesBy(1L);
          } else if (dir == Vote.Direction.UP) {
            shard.increaseVotesBy(2L);
          }
          break;
      }

      ofy().transact(() -> ofy().save().entities(dbVote.withDir(dir), shard).now());
    }
  }

  private Vote getVote(Key<? extends Votable> votableKey, Key<Account> accountKey) {
    return ofy().load().key(Vote.createKey(votableKey, accountKey)).now();
  }

  private Shardable.Shard getShard(Key<? extends Votable> votableKey) {
    Key<?> shardKey;
    if (votableKey.getKind().equals(Key.getKind(Post.class))) {
      shardKey = postShardService
          .getRandomShardKey(Key.create(votableKey.getRaw()));
    } else {
      shardKey = commentShardService
          .getRandomShardKey(Key.create(votableKey.getRaw()));
    }
    return (Shardable.Shard) ofy().load().key(shardKey).now();
  }
}
