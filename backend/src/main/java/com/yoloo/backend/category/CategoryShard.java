package com.yoloo.backend.category;

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
public class CategoryShard implements Shardable.Shard {

  /**
   * CategoryId:shardNum
   */
  @Id
  private String id;

  private long posts;

  public static Key<CategoryShard> createKey(Key<Category> categoryKey, int shardNum) {
    return Key.create(CategoryShard.class, categoryKey.toWebSafeString() + ":" + shardNum);
  }

  public Key<CategoryShard> getKey() {
    return Key.create(CategoryShard.class, id);
  }

  public void increasePosts() {
    posts++;
  }

  public void decreasePosts() {
    posts--;
  }

  public CategoryShard addPost(long post) {
    this.posts += post;
    return this;
  }

  @Override public void increaseVotesBy(long value) {

  }

  @Override public void decreaseVotesBy(long value) {

  }
}
