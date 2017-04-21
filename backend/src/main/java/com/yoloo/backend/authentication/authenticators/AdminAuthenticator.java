package com.yoloo.backend.authentication.authenticators;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class AdminAuthenticator implements Authenticator {

  @Override
  public User authenticate(HttpServletRequest request) {
    String authHeader = request.getHeader(OAuth2.HeaderType.AUTHORIZATION);

    if (Strings.isNullOrEmpty(authHeader)) {
      return null;
    }

    if (authHeader.split(" ")[1].equals(Constants.ADMIN_EMAIL)) {
      return new User("1", Constants.ADMIN_EMAIL);
    }

    return null;
  }
}
