package com.yoloo.backend.vote;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.comment.CommentShard;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.post.PostShard;
import com.yoloo.backend.post.PostShardService;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public final class VoteController extends Controller {

  private static final Logger LOG =
      Logger.getLogger(VoteController.class.getName());

  private PostShardService postShardService;

  private CommentShardService commentShardService;

  public void votePost(String postId, Vote.Direction direction, User user) {
    Key<Votable> votableKey = Key.create(postId);
    Key<Account> accountKey = Key.create(user.getUserId());

    try {
      Vote vote = ofy().load().key(Vote.createKey(votableKey, accountKey)).safe();

      PostShard shard = getQuestionShard(votableKey);

      switch (vote.getDir()) {
        case DEFAULT:
          if (direction.equals(Vote.Direction.UP)) {
            shard.increaseVotes();
          } else if (direction.equals(Vote.Direction.DOWN)) {
            shard.decreaseVotes();
          }
          break;
        case UP:
          if (direction.equals(Vote.Direction.DEFAULT)) {
            shard.decreaseVotes();
          } else if (direction.equals(Vote.Direction.DOWN)) {
            shard.decreaseVotes();
            shard.decreaseVotes();
          }
          break;
        case DOWN:
          if (direction.equals(Vote.Direction.DEFAULT)) {
            shard.increaseVotes();
          } else if (direction.equals(Vote.Direction.UP)) {
            shard.increaseVotes();
            shard.increaseVotes();
          }
          break;
      }

      ofy().transact(() -> ofy().save().entities(vote.withDir(direction), shard).now());
    } catch (NotFoundException e) {
      Vote vote = Vote.builder()
          .id(votableKey.toWebSafeString())
          .parent(accountKey)
          .votableKey(votableKey)
          .dir(direction)
          .build();

      PostShard shard = getQuestionShard(votableKey);

      switch (direction) {
        case UP:
          shard.increaseVotes();
          break;
        case DOWN:
          shard.decreaseVotes();
          break;
      }

      ofy().transact(() -> ofy().save().entities(vote.withDir(direction), shard).now());
    }
  }

  public void voteComment(String commentId, Vote.Direction direction, User user) {
    Key<Votable> votableKey = Key.create(commentId);
    Key<Account> accountKey = Key.create(user.getUserId());

    try {
      Vote vote = ofy().load().key(Vote.createKey(votableKey, accountKey)).safe();

      CommentShard shard = getCommentShard(votableKey);

      switch (vote.getDir()) {
        case DEFAULT:
          if (direction.equals(Vote.Direction.UP)) {
            shard.increaseVotes();
          } else if (direction.equals(Vote.Direction.DOWN)) {
            shard.decreaseVotes();
          }
          break;
        case UP:
          if (direction.equals(Vote.Direction.DEFAULT)) {
            shard.decreaseVotes();
          } else if (direction.equals(Vote.Direction.DOWN)) {
            shard.decreaseVotes();
            shard.decreaseVotes();
          }
          break;
        case DOWN:
          if (direction.equals(Vote.Direction.DEFAULT)) {
            shard.increaseVotes();
          } else if (direction.equals(Vote.Direction.UP)) {
            shard.increaseVotes();
            shard.increaseVotes();
          }
          break;
      }

      ofy().transact(() -> ofy().save().entities(vote.withDir(direction), shard).now());
    } catch (NotFoundException e) {
      Vote vote = Vote.builder()
          .id(votableKey.toWebSafeString())
          .parent(accountKey)
          .votableKey(votableKey)
          .dir(direction)
          .build();

      CommentShard shard = getCommentShard(votableKey);

      switch (direction) {
        case UP:
          shard.increaseVotes();
          break;
        case DOWN:
          shard.decreaseVotes();
          break;
      }

      ofy().transact(() -> ofy().save().entities(vote.withDir(direction), shard).now());
    }
  }

  private CommentShard getCommentShard(Key<Votable> votableKey) {
    Key<CommentShard> shardKey = commentShardService
        .getRandomShardKey(Key.create(votableKey.getRaw()));
    return ofy().load().key(shardKey).now();
  }

  private PostShard getQuestionShard(Key<Votable> votableKey) {
    Key<PostShard> shardKey = postShardService
        .getRandomShardKey(Key.create(votableKey.getRaw()));
    return ofy().load().key(shardKey).now();
  }
}
