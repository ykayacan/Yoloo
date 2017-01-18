package com.yoloo.backend.topic;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.config.ShardConfig;
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
public class TopicCounterShard {

  public static final int SHARD_COUNT = ShardConfig.CATEGORY_SHARD_COUNTER;

  /**
   * Websafe categoryId:shard_num
   */
  @Id
  private String id;

  private long questions;

  public static Key<TopicCounterShard> createKey(Key<Topic> topicKey, int shardId) {
    return Key.create(TopicCounterShard.class, topicKey.toWebSafeString() + ":" + shardId);
  }

  public Key<TopicCounterShard> getKey() {
    return Key.create(TopicCounterShard.class, id);
  }

  public void increaseQuestions() {
    this.questions++;
  }

  public void decreaseQuestions() {
    this.questions--;
  }

  public TopicCounterShard addQuestions(long questions) {
    this.questions += questions;
    return this;
  }
}
