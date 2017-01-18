package com.yoloo.backend.feed;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
import java.util.logging.Logger;
import javax.annotation.Nullable;
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
    resource = "accounts",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID,},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class FeedEndpoint {

  private static final Logger logger =
      Logger.getLogger(FeedEndpoint.class.getSimpleName());

  private final FeedController feedController = FeedControllerFactory.of().create();

  /**
   * Feed collection response.
   *
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.feed",
      path = "accounts/feed",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<FeedItem> list(@Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit, User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return feedController
        .list(Optional.fromNullable(limit), Optional.fromNullable(cursor), user);
  }
}
