package com.yoloo.backend.validator.rule.token;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

public class GrantTypeRule implements Rule<BadRequestException> {

    private final String grantType;

    public GrantTypeRule(String grantType) {
        this.grantType = grantType;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(grantType)) {
            throw new BadRequestException("Parameters missing from the request : grant_type");
        }
    }
}
