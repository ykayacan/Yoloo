package com.yoloo.backend.validator.rule.follow;

import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.validator.Rule;

public class FollowNotFoundRule implements Rule<NotFoundException> {

    private final String websafeFolloweeId;
    private final User user;

    public FollowNotFoundRule(String websafeFolloweeId, User user) {
        this.websafeFolloweeId = websafeFolloweeId;
        this.user = user;
    }

    @Override
    public void validate() throws NotFoundException {
        try {
            Key<Account> followerKey = Key.create(user.getUserId());
            Key<Account> followeeKey = Key.create(websafeFolloweeId);

            /*ofy().load().type(Follow.class)
                    .ancestor(followerKey)
                    .filter("followeeKey =", followeeKey)
                    .keys().first().safe();*/
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Follow.");
        }
    }
}
