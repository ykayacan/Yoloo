package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.validator.Rule;

public class AllowedToOperate implements Rule<ForbiddenException> {

    private final User user;
    private final String websafeEntityId;
    private final Operation operation;

    public AllowedToOperate(User user, String websafeEntityId, Operation operation) {
        this.user = user;
        this.websafeEntityId = websafeEntityId;
        this.operation = operation;
    }

    @Override
    public void validate() throws ForbiddenException {
        Key<?> key = Key.create(websafeEntityId);
        Key<Account> userKey = Key.create(user.getUserId());

        if (key.getParent().compareTo(userKey) != 0) {
            throw new ForbiddenException("You don't have permissions to " +
                    operation.toString() + " on " + key.getKind().toLowerCase());
        }
    }

    public enum Operation {
        UPDATE, DELETE
    }
}
