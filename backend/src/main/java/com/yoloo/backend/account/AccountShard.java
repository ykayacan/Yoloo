package com.yoloo.backend.account;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotZero;
import com.yoloo.backend.config.ShardConfig;
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
public final class AccountShard implements Shardable.Shard {

  public static final int SHARD_COUNT = ShardConfig.ACCOUNT_SHARD_COUNTER;

  /**
   * Websafe accountId:shard_num
   */
  @Id
  private String id;

  @Index(IfNotZero.class)
  private long followingCount;

  @Index(IfNotZero.class)
  private long followerCount;

  @Index(IfNotZero.class)
  private long postCount;

  public static Key<AccountShard> createKey(Key<Account> accountKey, int shardId) {
    return Key.create(AccountShard.class, accountKey.toWebSafeString() + ":" + shardId);
  }

  public Key<AccountShard> getKey() {
    return Key.create(AccountShard.class, id);
  }

  public String getWebsafeAccountId() {
    return id.split(":")[0];
  }

  public long increaseFollowings() {
    return ++followingCount;
  }

  public long decreaseFollowings() {
    return --followingCount;
  }

  public long increaseFollowers() {
    return ++followerCount;
  }

  public long decreaseFollowers() {
    return --followerCount;
  }

  public long increasePostCount() {
    return ++postCount;
  }

  public long decreasePostCount() {
    return --postCount;
  }

  @Override public void increaseVotesBy(long value) {

  }

  @Override public void decreaseVotesBy(long value) {

  }
}