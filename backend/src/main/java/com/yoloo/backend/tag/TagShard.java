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
public class TagShard {

  public static final int SHARD_COUNT = ShardConfig.TAG_SHARD_COUNTER;

  /**
   * TagId:shardNum
   */
  @Id
  private String id;

  private long posts;

  public static Key<TagShard> createKey(Key<Tag> tagKey, int shardNum) {
    return Key.create(TagShard.class, tagKey.toWebSafeString() + ":" + shardNum);
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<TagShard> getKey() {
    return Key.create(TagShard.class, id);
  }

  public void increasePosts() {
    posts++;
  }

  public void decreasePosts() {
    posts--;
  }
}
