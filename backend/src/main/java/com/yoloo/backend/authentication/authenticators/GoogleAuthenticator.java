package com.yoloo.backend.authentication.authenticators;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.appengine.repackaged.com.google.api.client.http.javanet.NetHttpTransport;
import com.google.appengine.repackaged.com.google.api.client.json.jackson2.JacksonFactory;

import com.googlecode.objectify.Key;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.account.Account;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.backend.OfyService.ofy;

public class GoogleAuthenticator implements Authenticator {

    private static GoogleIdTokenVerifier getVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(Constants.WEB_CLIENT_ID))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    public static GoogleIdToken.Payload processGoogleToken(final String idToken) {
        final GoogleIdToken googleIdToken;
        try {
            googleIdToken = getVerifier().verify(idToken);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            return null;
        }

        if (googleIdToken != null) {
            return googleIdToken.getPayload();
        }

        return null;
    }

    @Override
    public User authenticate(final HttpServletRequest request) {
        final String authzHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

        if (Strings.isNullOrEmpty(authzHeader) ||
                !authzHeader.contains(OAuth2.OAUTH_HEADER_NAME)) {
            return null;
        }

        final String accessToken = authzHeader.substring(6).trim();

        final GoogleIdToken.Payload payload = processGoogleToken(accessToken);
        if (payload == null) {
            return null;
        }

        final Key<Account> accountKey = getAccountKeyByEmail(payload.getEmail());
        if (accountKey == null) {
            return null;
        }

        return new User(accountKey.toWebSafeString(), "");
    }

    private static Key<Account> getAccountKeyByEmail(final String email) {
        return ofy().load().type(Account.class)
                .filter("email", email)
                .keys().first().now();
    }
}
