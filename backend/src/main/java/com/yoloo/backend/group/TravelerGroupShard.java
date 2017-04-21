package com.yoloo.backend.group;

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
public class TravelerGroupShard implements Shardable.Shard {

  /**
   * CategoryId:shardNum
   */
  @Id
  private String id;

  private long posts;

  public static Key<TravelerGroupShard> createKey(Key<TravelerGroupEntity> categoryKey, int shardNum) {
    return Key.create(TravelerGroupShard.class, categoryKey.toWebSafeString() + ":" + shardNum);
  }

  public Key<TravelerGroupShard> getKey() {
    return Key.create(TravelerGroupShard.class, id);
  }

  public void increasePosts() {
    posts++;
  }

  public void decreasePosts() {
    posts--;
  }

  public TravelerGroupShard addPost(long post) {
    this.posts += post;
    return this;
  }

  @Override public void increaseVotesBy(long value) {

  }

  @Override public void decreaseVotesBy(long value) {

  }
}
