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
import com.yoloo.backend.authentication.authenticators.AdminAuthenticator;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.post.PostEntity;
import javax.annotation.Nullable;
import javax.inject.Named;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod.GET;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace =
    @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER
    ))
@ApiClass(
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID, },
    authenticators = { FirebaseAuthenticator.class, AdminAuthenticator.class }
)
public class FeedEndpoint {

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
  @ApiMethod(name = "users.me.feed", path = "users/me/feed", httpMethod = GET)
  public CollectionResponse<PostEntity> list(@Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit, User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return feedController.listFeed(Optional.fromNullable(limit), Optional.fromNullable(cursor),
        user);
  }

  @ApiMethod(name = "admin.feed", path = "admin/feed", httpMethod = GET)
  public void createFeedForAllUsers(User user) throws ServiceException {
    EndpointsValidator.create().on(AuthValidator.create(user));

    feedController.createFeed();
  }
}
