package com.yoloo.backend.validator.rule.follow;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.follow.Follow;
import com.yoloo.backend.validator.Rule;

import static com.yoloo.backend.OfyService.ofy;

public class FollowConflictRule implements Rule<ConflictException> {

  private final String followingId;
  private final User user;

  /**
   * Instantiates a new Follow conflict rule.
   *
   * @param followingId the following id
   * @param user the user
   */
  public FollowConflictRule(String followingId, User user) {
    this.followingId = followingId;
    this.user = user;
  }

  @Override
  public void validate() throws ConflictException {
    Key<Account> followerKey = Key.create(user.getUserId());
    Key<Account> followingKey = Key.create(followingId);

    if (followingKey.equivalent(followerKey)) {
      throw new ConflictException("You can't follow yourself.");
    }

    final Key<Follow> followKey = ofy().load().type(Follow.class)
        .ancestor(followerKey).filter(Follow.FIELD_FOLLOWING_KEY + " =", followingKey)
        .keys().first().now();

    if (followKey != null) {
      throw new ConflictException("Already following.");
    }
  }
}
