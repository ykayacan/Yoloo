package com.yoloo.backend.validator.rule.credentials;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

public class CredentialsRule implements Rule<BadRequestException> {

    private  final String credentials;

    public CredentialsRule(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(credentials)) {
            throw new BadRequestException("Parameters missing from the request : credentials");
        }
    }
}
