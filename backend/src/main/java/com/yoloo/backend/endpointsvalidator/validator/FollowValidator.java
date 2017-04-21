package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.endpointsvalidator.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class FollowValidator implements Validator {

  private String followerId;
  private String followingId;

  @Override
  public boolean isValid() {
    final Key<Account> followerKey = Key.create(followerId);
    final Key<Account> followingKey = Key.create(followingId);

    return !followingKey.equivalent(followerKey);
  }

  @Override
  public void onException() throws ServiceException {
    throw new BadRequestException("You can not do the action for yourself.");
  }
}
