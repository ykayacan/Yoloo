package com.yoloo.backend.validator.rule.token;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.Constants;
import com.yoloo.backend.validator.Rule;

public class ClientIdRule implements Rule<BadRequestException> {

    private final String clientId;

    public ClientIdRule(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(clientId)) {
            throw new BadRequestException("Parameters missing from the request : client_id");
        }

        if (!isValidClientId(clientId)) {
            throw new BadRequestException("No client found for the provided id.");
        }
    }

    private boolean isValidClientId(final String clientId) {
        return clientId.equals(Constants.BASE64_CLIENT_ID);
    }
}
