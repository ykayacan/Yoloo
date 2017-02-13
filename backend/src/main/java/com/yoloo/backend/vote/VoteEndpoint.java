package com.yoloo.backend.vote;

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

  private static final Logger logger =
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

    Validator.builder()
        .addRule(new IdValidationRule(postId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(postId))
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

    Validator.builder()
        .addRule(new IdValidationRule(commentId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(commentId))
        .validate();

    voteController.voteComment(commentId, direction, user);
  }
}
