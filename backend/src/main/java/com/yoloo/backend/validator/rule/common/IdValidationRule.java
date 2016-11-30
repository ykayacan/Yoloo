package com.yoloo.backend.validator.rule.common;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

public class IdValidationRule implements Rule<BadRequestException> {

    private final String websafeKey;

    public IdValidationRule(final String websafeKey) {
        this.websafeKey = websafeKey;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(websafeKey)) {
            throw new BadRequestException(
                    "Given item id " + websafeKey + " is invalid.");
        }
    }
}
