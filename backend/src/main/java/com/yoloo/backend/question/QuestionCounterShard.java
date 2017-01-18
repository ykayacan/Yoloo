package com.yoloo.backend.question;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.feed.FeedShard;
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
public final class QuestionCounterShard implements FeedShard {

  public static final int SHARD_COUNT = ShardConfig.QUESTION_SHARD_COUNTER;

  /**
   * Websafe questionId:shard_num
   */
  @Id
  private String id;

  private long comments;

  private long votes;

  private int reports;

  public static Key<QuestionCounterShard> createKey(Key<Question> questionKey, int shardId) {
    return Key.create(QuestionCounterShard.class, questionKey.toWebSafeString() + ":" + shardId);
  }

  public Key<QuestionCounterShard> getKey() {
    return Key.create(QuestionCounterShard.class, this.id);
  }

  public void increaseVotes() {
    this.votes++;
  }

  public void decreaseVotes() {
    this.votes--;
  }

  public void increaseComments() {
    this.comments++;
  }

  public void decreaseComments() {
    this.comments--;
  }

  public QuestionCounterShard addValues(long comments, long votes, int reports) {
    this.comments += comments;
    this.votes += votes;
    this.reports += reports;
    return this;
  }
}