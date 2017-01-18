package com.yoloo.backend.validator.rule.common;

import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.validator.Guard;
import com.yoloo.backend.validator.Rule;

public class ForbiddenValidator implements Rule<ForbiddenException> {

  private final User user;
  private final String entityId;
  private final Operation operation;

  public ForbiddenValidator(User user, String entityId, Operation operation) {
    this.user = user;
    this.entityId = entityId;
    this.operation = operation;
  }

  @Override
  public void validate() throws ForbiddenException {
    Key<?> key = Key.create(entityId);
    Key<Account> userKey = Key.create(user.getUserId());

    Guard.checkForbiddenRequest(key.<Account>getParent().equivalent(userKey),
        "Not allowed to " + operation.toString().toLowerCase());
  }

  public enum Operation {
    UPDATE, DELETE
  }
}
