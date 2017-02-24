package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.yoloo.backend.endpointsvalidator.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class AuthValidator implements Validator {

  private User user;

  @Override public boolean valid() {
    return !(user == null || user.getUserId() == null);
  }

  @Override public void onException() throws ServiceException {
    throw new UnauthorizedException("Only authenticated users may invoke this operation.");
  }
}
