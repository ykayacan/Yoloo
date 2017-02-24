package com.yoloo.backend.follow;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.FollowConflictValidator;
import com.yoloo.backend.endpointsvalidator.validator.FollowValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import javax.inject.Named;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER,
        packagePath = Constants.API_PACKAGE_PATH
    )
)
@ApiClass(
    resource = "postCount",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class FollowEndpoint {

  private final FollowController followController = FollowControllerFactory.of().create();

  /**
   * Follows a new {@code Account}.
   *
   * @param accountId the websafe account id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "follows.follow",
      path = "accounts/{accountId}/follows",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void follow(@Named("accountId") String accountId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .on(FollowValidator.create(user.getUserId(), accountId))
        .on(FollowConflictValidator.create(user.getUserId(), accountId, "Already following."))
        .validate();

    followController.follow(accountId, user);
  }

  /**
   * Unfollows the specified {@code Account}.
   *
   * @param accountId the ID of the entity to deletePost
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "follows.unfollow",
      path = "accounts/{accountId}/follows",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void unfollow(@Named("accountId") String accountId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .validate();

    followController.unfollow(accountId, user);
  }
}
