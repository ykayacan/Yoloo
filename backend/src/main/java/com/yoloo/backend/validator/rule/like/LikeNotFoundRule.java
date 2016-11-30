package com.yoloo.backend.validator.rule.like;

import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.validator.Rule;

public class LikeNotFoundRule implements Rule<NotFoundException> {

    private final String websafePostId;
    private final User user;

    public LikeNotFoundRule(String websafePostId, User user) {
        this.websafePostId = websafePostId;
        this.user = user;
    }

    @Override
    public void validate() throws NotFoundException {
        try {
            Key<Account> userKey = Key.create(user.getUserId());
            /*Key<? extends Likeable> likeableKey = Key.createShards(websafePostId);

            ofy().load().type(Like.class).ancestor(userKey)
                    .filter("likeableEntityKey =", likeableKey)
                    .keys().first().safe();*/
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Like.");
        }
    }
}
