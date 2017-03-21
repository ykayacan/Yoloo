package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.endpointsvalidator.Validator;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class ForbiddenValidator implements Validator {

  private String itemId;
  private User user;
  private Op operation;

  @Override public boolean isValid() {
    try {
      final Key<?> key = Key.create(itemId);
      final Key<Account> userKey = Key.create(user.getUserId());

      return key.<Account>getParent().equivalent(userKey);
    } catch (Exception e) {
      return false;
    }
  }

  @Override public void onException() throws ServiceException {
    throw new ForbiddenException("Not has permission to "
        + operation.toString().toLowerCase()
        + ".");
  }

  public enum Op {
    UPDATE, DELETE
  }
}
