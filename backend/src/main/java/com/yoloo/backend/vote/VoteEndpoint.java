package com.yoloo.backend.vote;

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
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
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
    resource = "votes",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class VoteEndpoint {

  private static final Logger LOG =
      Logger.getLogger(VoteEndpoint.class.getSimpleName());

  private final VoteController voteController = VoteControllerFactory.of().create();

  /**
   * Vote {@code Post} with given ID.
   *
   * @param postId the websafe votable id
   * @param direction the direction
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.vote",
      path = "posts/{postId}/votes",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void votePost(
      @Named("postId") String postId,
      @Named("dir") Vote.Direction direction,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."))
        .validate();

    voteController.votePost(postId, direction, user);
  }

  /**
   * Vote {@code Comment} with given ID.
   *
   * @param commentId the websafe votable id
   * @param direction the direction
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "comments.vote",
      path = "comments/{commentId}/votes",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void voteComment(
      @Named("commentId") String commentId,
      @Named("dir") Vote.Direction direction,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(commentId, "commentId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(commentId, "Invalid commentId."))
        .validate();

    voteController.voteComment(commentId, direction, user);
  }
}
