package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

import com.yoloo.backend.validator.Rule;

import java.util.logging.Logger;

public class AuthenticationRule implements Rule<UnauthorizedException> {

    private static final Logger logger =
            Logger.getLogger(AuthenticationRule.class.getName());

    private final User user;

    public AuthenticationRule(User user) {
        this.user = user;
    }

    @Override
    public void validate() throws UnauthorizedException {
        if (user == null || user.getUserId() == null) {
            throw new UnauthorizedException(
                    "Only authenticated users may invoke this operation.");
        }
    }
}
