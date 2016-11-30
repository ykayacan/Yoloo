package com.yoloo.backend.validator.rule.comment;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

public class CommentCreateRule implements Rule<BadRequestException> {

    private final String comment;

    public CommentCreateRule(String comment) {
        this.comment = comment;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(comment)) {
            throw new BadRequestException("Parameters missing from the request : text");
        }
    }
}
