package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.yoloo.backend.validator.Rule;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthValidator implements Rule<UnauthorizedException> {

  private final User user;

  @Override
  public void validate() throws UnauthorizedException {
    if (this.user == null || this.user.getUserId() == null) {
      throw new UnauthorizedException("Only authenticated users may invoke this operation.");
    }
  }
}
