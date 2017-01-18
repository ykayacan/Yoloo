package com.yoloo.backend.tag;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
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
import lombok.experimental.Wither;

@Entity
@Cache
@Data
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagCounterShard {

  public static final int SHARD_COUNT = ShardConfig.HASHTAG_SHARD_COUNTER;

  /**
   * Websafe tagId:shard_num
   */
  @Id
  private String id;

  private long questions;

  public static Key<TagCounterShard> createKey(Key<Tag> tagKey, int shardId) {
    return Key.create(TagCounterShard.class, tagKey.toWebSafeString() + ":" + shardId);
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<TagCounterShard> getKey() {
    return Key.create(TagCounterShard.class, id);
  }

  public void increaseQuestions() {
    this.questions++;
  }

  public void decreaseQuestions() {
    this.questions--;
  }
}
