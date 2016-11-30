package com.yoloo.backend.validator.rule.credentials;

import com.google.api.server.spi.response.ConflictException;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.validator.Rule;

import static com.yoloo.backend.OfyService.ofy;

public class UserConflictRule implements Rule<ConflictException> {

    private final String email;
    private final String username;

    public UserConflictRule(String username, String email) {
        this.email = email;
        this.username = username;
    }

    @Override
    public void validate() throws ConflictException {
        final Key<Account> email = ofy().load().type(Account.class)
                .filter("email =", this.email)
                .keys().first().now();

        if (email != null) {
            throw new ConflictException("Email is already taken.");
        }

        final Key<Account> username = ofy().load().type(Account.class)
                .filter("username =", this.username)
                .keys().first().now();

        if (username != null) {
            throw new ConflictException("Username is already taken.");
        }
    }
}
