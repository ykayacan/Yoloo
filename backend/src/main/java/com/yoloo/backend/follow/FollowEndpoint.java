package com.yoloo.backend.follow;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;
import com.yoloo.backend.validator.rule.follow.FollowConflictRule;
import com.yoloo.backend.validator.rule.follow.FollowNotFoundRule;
import java.util.logging.Logger;
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
    resource = "questions",
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

  private static final Logger logger =
      Logger.getLogger(FollowEndpoint.class.getSimpleName());

  private final FollowController followController = FollowContollerFactory.of().create();

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

    Validator.builder()
        .addRule(new IdValidationRule(accountId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(accountId))
        .addRule(new FollowConflictRule(accountId, user))
        .validate();

    followController.follow(accountId, user);
  }

  /**
   * Unfollows the specified {@code Account}.
   *
   * @param accountId the ID of the entity to delete
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "follows.unfollow",
      path = "accounts/{accountId}/follows",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void unfollow(@Named("accountId") String accountId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(accountId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(accountId))
        .addRule(new FollowNotFoundRule(accountId, user))
        .validate();

    followController.unfollow(accountId, user);
  }
}
