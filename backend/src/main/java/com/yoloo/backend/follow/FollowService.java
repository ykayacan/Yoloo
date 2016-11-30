package com.yoloo.backend.follow;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "newInstance")
public class FollowService {

    public Follow create(Key<Account> followerKey, Key<Account> followingKey) {
        return Follow.builder()
                .parentUserKey(followerKey)
                .followingKey(followingKey)
                .build();
    }
}
