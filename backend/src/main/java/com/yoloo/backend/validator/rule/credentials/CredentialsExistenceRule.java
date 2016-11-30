package com.yoloo.backend.validator.rule.credentials;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

public class CredentialsExistenceRule implements Rule<ServiceException> {

    private String[] values;

    public CredentialsExistenceRule(String[] values) {
        this.values = values;
    }

    @Override
    public void validate() throws ServiceException {
        if (values.length != 3) {
            throw new BadRequestException("Invalid credentials.");
        }
    }
}
