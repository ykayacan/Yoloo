package com.yoloo.backend.comment;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.shard.Shardable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Cache(expirationSeconds = 60)
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentShard implements Shardable.Shard {

  /**
   * Websafe commentId:shard_num
   */
  @Id
  private String id;

  private long votes;

  public static Key<CommentShard> createKey(Key<Comment> commentKey, int shardId) {
    return Key.create(CommentShard.class, commentKey.toWebSafeString() + ":" + shardId);
  }

  public Key<CommentShard> getKey() {
    return Key.create(CommentShard.class, id);
  }

  @Override public void increaseVotesBy(long value) {
    votes += value;
  }

  @Override public void decreaseVotesBy(long value) {
    votes -= value;
  }

  @Deprecated
  public void increaseVotes() {
    ++this.votes;
  }

  @Deprecated
  public void decreaseVotes() {
    --this.votes;
  }

  public CommentShard addValues(long votes) {
    this.votes += votes;
    return this;
  }
}
