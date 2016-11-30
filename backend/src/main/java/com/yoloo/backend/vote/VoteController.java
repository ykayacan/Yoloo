package com.yoloo.backend.vote;

import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.VoidWork;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentCounterShard;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionCounterShard;
import com.yoloo.backend.question.QuestionShardService;

import java.util.logging.Logger;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "newInstance")
public final class VoteController extends Controller {

    private static final Logger logger =
            Logger.getLogger(VoteController.class.getName());

    @NonNull
    private QuestionShardService questionShardService;

    @NonNull
    private CommentShardService commentShardService;

    public void vote(String websafeVotableId, final Vote.Direction direction, User user) {
        Key<Votable> votableKey = Key.create(websafeVotableId);
        Key<Account> accountKey = Key.create(user.getUserId());

        // TODO: 28.11.2016 Change code structure.

        try {
            Vote vote = ofy().load().type(Vote.class)
                    .parent(accountKey)
                    .id(votableKey.toWebSafeString())
                    .safe();

            if (votableKey.getKind().equals(Comment.class.getSimpleName())) {
                Key<Comment> commentKey = Key.create(websafeVotableId);
                Key<CommentCounterShard> shardKey =
                        commentShardService.getRandomShardKey(commentKey);

                CommentCounterShard shard = ofy().load().key(shardKey).now();

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

                ofy().save().entities(shard, vote.withDir(direction)).now();

            } else if (votableKey.getKind().equals(Question.class.getSimpleName())) {
                Key<Question> postKey = Key.create(websafeVotableId);
                Key<QuestionCounterShard> shardKey = questionShardService.getRandomShardKey(postKey);

                QuestionCounterShard shard = ofy().load().key(shardKey).now();

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

                ofy().save().entities(vote.withDir(direction), shard).now();
            }
        } catch (NotFoundException e) {
            Vote vote = Vote.builder()
                    .id(votableKey.toWebSafeString())
                    .parentUserKey(accountKey)
                    .votableKey(votableKey)
                    .dir(direction)
                    .build();

            if (votableKey.getKind().equals(Comment.class.getSimpleName())) {
                Key<Comment> commentKey = Key.create(websafeVotableId);
                Key<CommentCounterShard> shardKey =
                        commentShardService.getRandomShardKey(commentKey);

                CommentCounterShard shard = ofy().load().key(shardKey).now();

                switch (direction) {
                    case UP:
                        shard.increaseVotes();
                        break;
                    case DOWN:
                        shard.decreaseVotes();
                        break;
                }

                ofy().save().entities(vote.withDir(direction), shard).now();

            } else if (votableKey.getKind().equals(Question.class.getSimpleName())) {
                Key<Question> postKey = Key.create(websafeVotableId);
                Key<QuestionCounterShard> shardKey = questionShardService.getRandomShardKey(postKey);

                final QuestionCounterShard shard = ofy().load().key(shardKey).now();

                switch (direction) {
                    case UP:
                        shard.increaseVotes();
                        break;
                    case DOWN:
                        shard.decreaseVotes();
                        break;
                }

                final Vote finalVote = vote.withDir(direction);
                ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {
                        ofy().save().entities(finalVote, shard).now();
                    }
                });
            }
        }
    }
}
