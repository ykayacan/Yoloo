package com.yoloo.backend.validator.rule.token;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.response.BadRequestException;

import com.yoloo.backend.validator.Rule;

import java.util.regex.Pattern;

public class PasswordGrantTypeRule implements Rule<BadRequestException> {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private final String email;
    private final String password;

    public PasswordGrantTypeRule(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public void validate() throws BadRequestException {
        if (Strings.isNullOrEmpty(email)) {
            throw new BadRequestException("Parameters missing from the request : email");
        }

        if (Strings.isNullOrEmpty(password)) {
            throw new BadRequestException("Parameters missing from the request : password");
        }

        if (!isValidEmail(email)) {
            throw new BadRequestException("Invalid email");
        }
    }

    /**
     * Validate email with regular expression
     *
     * @param email hex for validation
     * @return true valid hex, false invalid hex
     */
    private boolean isValidEmail(final String email) {
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
}
