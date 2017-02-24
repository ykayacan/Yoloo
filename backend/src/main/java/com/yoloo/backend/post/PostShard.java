package com.yoloo.backend.post;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.feed.FeedShard;
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
public final class PostShard implements FeedShard, Shardable.Shard {

  /**
   * Websafe questionId:shard_num
   */
  @Id
  private String id;

  private long comments;

  private long votes;

  private int reports;

  public static Key<PostShard> createKey(Key<Post> postKey, int shardNum) {
    return Key.create(PostShard.class, postKey.toWebSafeString() + ":" + shardNum);
  }

  public Key<PostShard> getKey() {
    return Key.create(PostShard.class, this.id);
  }

  public void increaseComments() {
    this.comments++;
  }

  public void decreaseComments() {
    this.comments--;
  }

  public PostShard addValues(long comments, long votes, int reports) {
    this.comments += comments;
    this.votes += votes;
    this.reports += reports;
    return this;
  }

  @Override public void increaseVotesBy(long value) {
    votes += value;
  }

  @Override public void decreaseVotesBy(long value) {
    votes -= value;
  }
}