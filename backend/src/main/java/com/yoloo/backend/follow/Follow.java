package com.yoloo.backend.follow;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache(expirationSeconds = 120)
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Follow {

    /**
     * The constant FIELD_FOLLOWING_KEY.
     */
    public static final String FIELD_FOLLOWING_KEY = "followingKey";

    @Id
    private Long id;

    @Parent
    private Key<Account> parentUserKey;

    @Index
    @NonFinal
    private Key<Account> followingKey;
}