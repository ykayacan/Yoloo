package com.yoloo.backend.authentication.authenticators;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.yoloo.backend.authentication.Token;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import javax.servlet.http.HttpServletRequest;

import static com.yoloo.backend.OfyService.ofy;

public class YolooAuthenticator implements Authenticator {

  @Override
  public User authenticate(final HttpServletRequest request) {
    final String authzHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

    if (Strings.isNullOrEmpty(authzHeader) ||
        !authzHeader.contains(OAuth2.OAUTH_HEADER_NAME)) {
      return null;
    }

    final String accessToken = authzHeader.substring(6).trim();

    final Token token = ofy().load().type(Token.class)
        .filter("accessToken =", accessToken).first().now();
    if (token == null || token.isTokenExpired()) {
      return null;
    }

    return new User(token.getKey().getParent().toWebSafeString(), "");
  }
}
