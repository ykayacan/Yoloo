package com.yoloo.backend.endpointsvalidator.validator;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.endpointsvalidator.Validator;
import com.yoloo.backend.relationship.Relationship;
import lombok.AllArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor(staticName = "create")
public class FollowConflictValidator implements Validator {

  private String followerId;
  private String followingId;
  private String message;

  @Override
  public boolean isValid() {
    final Key<Account> followerKey = Key.create(followerId);
    final Key<Account> followingKey = Key.create(followingId);

    final Key<Relationship> followKey = ofy()
        .load()
        .type(Relationship.class)
        .ancestor(followerKey)
        .filter(Relationship.FIELD_FOLLOWING_KEY + " =", followingKey)
        .keys()
        .first()
        .now();

    return followKey == null;
  }

  @Override
  public void onException() throws ServiceException {
    throw new ConflictException(message);
  }
}
