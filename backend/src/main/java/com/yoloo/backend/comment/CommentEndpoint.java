package com.yoloo.backend.comment;

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
import com.yoloo.backend.validator.rule.comment.CommentCreateRule;
import com.yoloo.backend.validator.rule.common.AuthValidator;
import com.yoloo.backend.validator.rule.common.BadRequestValidator;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;
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
    resource = "comments",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class CommentEndpoint {

  private static final Logger logger =
      Logger.getLogger(CommentEndpoint.class.getSimpleName());

  private final CommentController commentController = CommentControllerFactory.of().create();

  /**
   * Get comment.
   *
   * @param questionId the question id
   * @param commentId the comment id
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.comments.get",
      path = "questions/{questionId}/comments/{commentId}",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Comment get(@Named("questionId") String questionId, @Named("commentId") String commentId,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(questionId, commentId))
        .addRule(new AuthValidator(user))
        .validate();

    return commentController.get(commentId, user);
  }

  /**
   * Inserts a new {@code Comment}.
   *
   * @param questionId the websafe question id
   * @param content the content
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.comments.add",
      path = "questions/{questionId}/comments",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Comment add(@Named("questionId") String questionId, @Named("content") String content,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new CommentCreateRule(content))
        .addRule(new NotFoundRule(questionId))
        .validate();

    return commentController.add(questionId, content, user);
  }

  /**
   * Updates an existing {@code Comment}.
   *
   * @param questionId the ID from the entity to be updated
   * @param commentId the websafe comment id
   * @param content the content
   * @param accepted the accepted
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.comments.update",
      path = "questions/{questionId}/comments/{commentId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Comment update(@Named("questionId") String questionId,
      @Named("commentId") String commentId, @Nullable @Named("content") String content,
      @Nullable @Named("accepted") Boolean accepted, User user) throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(questionId))
        .validate();

    return commentController.update(questionId, commentId, Optional.fromNullable(content),
        Optional.fromNullable(accepted), user);
  }

  /**
   * Deletes the specified {@code Comment}.
   *
   * @param questionId the ID from the entity to be updated
   * @param commentId the websafe comment id
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.comments.delete",
      path = "questions/{questionId}/comments/{commentId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("questionId") String questionId, @Named("commentId") String commentId,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(questionId))
        .validate();

    commentController.delete(questionId, commentId, user);
  }

  /**
   * List all {@code Comment} entities.
   *
   * @param questionId the websafe question id
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number of entries to return
   * @param user the user
   * @return a response that encapsulates the result list and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.comments.list",
      path = "questions/{questionId}/comments",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Comment> list(@Named("questionId") String questionId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(questionId))
        .validate();

    return commentController.list(questionId, Optional.fromNullable(cursor),
        Optional.fromNullable(limit), user);
  }
}
