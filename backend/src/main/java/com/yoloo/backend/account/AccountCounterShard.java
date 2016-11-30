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
public final class AccountCounterShard {

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

    public Key<AccountCounterShard> getKey() {
        return Key.create(AccountCounterShard.class, id);
    }

    public long calculateFollowings(long followings) {
        this.followings += followings;
        return this.followings;
    }

    public long calculateFollowers(long followers) {
        this.followers += followers;
        return this.followers;
    }

    public long calculateQuestions(long questions) {
        this.questions += questions;
        return this.questions;
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