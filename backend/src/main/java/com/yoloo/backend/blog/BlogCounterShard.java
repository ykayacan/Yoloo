package com.yoloo.backend.blog;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.yoloo.backend.config.ShardConfig;
import com.yoloo.backend.feed.FeedShard;
import lombok.Builder;
import lombok.Data;

@Entity
@Cache(expirationSeconds = 60)
@Data
@Builder
public class BlogCounterShard implements FeedShard {

  public static final int SHARD_COUNT = ShardConfig.BLOG_SHARD_COUNTER;

  /**
   * Websafe blogId:shard_num
   */
  @Id
  private String id;

  @Index
  private Key<Blog> parentBlogKey;

  private long comments;

  private long votes;

  private int reports;

  public static Key<BlogCounterShard> createKey(Key<Blog> blogKey, int shardId) {
    return Key.create(BlogCounterShard.class, blogKey.toWebSafeString() + ":" + shardId);
  }

  public Key<BlogCounterShard> getKey() {
    return Key.create(BlogCounterShard.class, this.id);
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

  public void increaseReports() {
    this.reports++;
  }

  public void decreaseReports() {
    this.reports--;
  }

  public BlogCounterShard addValues(long comments, long votes, int reports) {
    this.comments += comments;
    this.votes += votes;
    this.reports += reports;
    return this;
  }
}
