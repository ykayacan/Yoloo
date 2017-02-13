package com.yoloo.backend.account;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotDefault;
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
public final class AccountShard {

  public static final int SHARD_COUNT = ShardConfig.ACCOUNT_SHARD_COUNTER;

  /**
   * Websafe accountId:shard_num
   */
  @Id
  private String id;

  @Index(IfNotDefault.class)
  private long followings;

  @Index(IfNotDefault.class)
  private long followers;

  @Index(IfNotDefault.class)
  private long questions;

  public static Key<AccountShard> createKey(Key<Account> accountKey, int shardId) {
    return Key.create(AccountShard.class, accountKey.toWebSafeString() + ":" + shardId);
  }

  public Key<AccountShard> getKey() {
    return Key.create(AccountShard.class, id);
  }

  public String getWebsafeAccountId() {
    return this.id.split(":")[0];
  }

  public long increaseFollowings() {
    return ++followings;
  }

  public long decreaseFollowings() {
    return --followings;
  }

  public long increaseFollowers() {
    return ++followers;
  }

  public long decreaseFollowers() {
    return --followers;
  }

  public long increaseQuestions() {
    return ++questions;
  }

  public long decreaseQuestions() {
    return --questions;
  }
}