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
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.CommentUpdateValidator;
import com.yoloo.backend.endpointsvalidator.validator.ForbiddenValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER)
)
@ApiClass(
    resource = "comments",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID },
    authenticators = { FirebaseAuthenticator.class }
)
public class CommentEndpoint {

  private static final Logger LOG =
      Logger.getLogger(CommentEndpoint.class.getSimpleName());

  private final CommentController commentController = CommentControllerFactory.of().create();

  /**
   * Get comment.
   *
   * @param postId the post id
   * @param commentId the comment id
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.comments.get",
      path = "posts/{postId}/comments/{commentId}",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Comment get(
      @Named("postId") String postId,
      @Named("commentId") String commentId,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(BadRequestValidator.create(commentId, "commentId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."));

    return commentController.getComment(commentId, user);
  }

  /**
   * Inserts a new {@code Comment}.
   *
   * @param postId the websafe question id
   * @param content the content
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.comments.insert",
      path = "posts/{postId}/comments",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Comment insert(
      @Named("postId") String postId,
      @Named("content") String content,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(BadRequestValidator.create(content, "content is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."));

    return commentController.insertComment(postId, content, user);
  }

  /**
   * Updates an existing {@code Comment}.
   *
   * @param postId the ID from the entity to be updated
   * @param commentId the websafe comment id
   * @param content the content
   * @param accepted the accepted
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.comments.update",
      path = "posts/{postId}/comments/{commentId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Comment update(
      @Named("postId") String postId,
      @Named("commentId") String commentId,
      @Nullable @Named("content") String content,
      @Nullable @Named("accepted") Boolean accepted,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(BadRequestValidator.create(commentId, "commentId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."))
        .on(CommentUpdateValidator.create(postId, commentId, user));

    return commentController.updateComment(
        postId,
        commentId,
        Optional.fromNullable(content),
        Optional.fromNullable(accepted),
        user);
  }

  /**
   * Delete.
   *
   * @param postId the post id
   * @param commentId the comment id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.comments.delete",
      path = "posts/{postId}/comments/{commentId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(
      @Named("postId") String postId,
      @Named("commentId") String commentId,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(BadRequestValidator.create(commentId, "commentId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."))
        .on(ForbiddenValidator.create(commentId, user, ForbiddenValidator.Op.DELETE));

    commentController.deleteComment(postId, commentId, user);
  }

  /**
   * List all {@code Comment} entities.
   *
   * @param postId the websafe question id
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number of entries to return
   * @param user the user
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.comments.list",
      path = "posts/{postId}/comments",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Comment> list(
      @Named("postId") String postId,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user)
      throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."));

    return commentController.listComments(
        postId,
        Optional.fromNullable(cursor),
        Optional.fromNullable(limit),
        user);
  }
}
