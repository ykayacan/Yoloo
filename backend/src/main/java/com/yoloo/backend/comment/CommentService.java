package com.yoloo.backend.comment;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.vote.Vote;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class CommentService {

  private CommentShardService shardService;

  public CommentModel create(Account account, Key<Question> questionKey, String content) {
    final Key<Comment> commentKey = factory().allocateId(account.getKey(), Comment.class);

    // Create list of new shard entities for given comment.
    List<CommentCounterShard> shards = shardService.createShards(commentKey);
    // Create list of new shard refs.
    List<Ref<CommentCounterShard>> shardRefs = ShardUtil.createRefs(shards)
        .toList(shards.size()).blockingGet();

    // Create new comment.
    Comment comment = Comment.builder()
        .id(commentKey.getId())
        .parentUserKey(account.getKey())
        .questionKey(questionKey)
        .shardRefs(shardRefs)
        .content(content)
        .username(account.getUsername())
        .avatarUrl(account.getAvatarUrl())
        .dir(Vote.Direction.DEFAULT)
        .accepted(false)
        .votes(0)
        .created(DateTime.now())
        .build();

    return CommentModel.builder().comment(comment).shards(shards).build();
  }

  public Comment update(Comment comment, Optional<String> content) {
    if (content.isPresent()) {
      comment = comment.withContent(content.get());
    }

    return comment;
  }

  public Pair<Question, Comment> accept(Question question, Comment comment,
      Optional<Boolean> accepted, Key<Account> accountKey) {
    if (accepted.isPresent() && question.getParentUserKey().equivalent(accountKey)) {
      question = question.withAcceptedCommentKey(comment.getKey());
      comment = comment.withAccepted(true);
    }

    return Pair.of(question, comment);
  }

  public List<Key<Vote>> getVoteKeys(Iterable<Key<Comment>> iterable) {
    Query<Vote> query = ofy().load().type(Vote.class);

    for (Key<Comment> commentKey : iterable) {
      query = query.filter(Vote.FIELD_VOTABLE_KEY + " =", commentKey);
    }

    return query.keys().list();
  }
}
