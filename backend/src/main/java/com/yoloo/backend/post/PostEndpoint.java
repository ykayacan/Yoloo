package com.yoloo.backend.post;

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
import com.yoloo.backend.endpointsvalidator.validator.ForbiddenValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import com.yoloo.backend.post.sort_strategy.PostSorter;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER
    ))
@ApiClass(
    resource = "posts",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID },
    authenticators = {
        FirebaseAuthenticator.class
    })
public class PostEndpoint {

  private static final Logger LOG =
      Logger.getLogger(PostEndpoint.class.getSimpleName());

  private final PostController postController = PostControllerFactory.of().create();

  /**
   * Returns the {@code Post} with the corresponding ID.
   *
   * @param postId the ID from the entity to be retrieved
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.get",
      path = "posts/{postId}",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Post get(@Named("postId") String postId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .validate();

    return postController.getPost(postId, user);
  }

  /**
   * Deletes the specified {@code Post}.
   *
   * @param postId the ID from the entity to delete
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.delete",
      path = "posts/{postId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("postId") String postId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."))
        .on(ForbiddenValidator.create(postId, user, ForbiddenValidator.Op.DELETE))
        .validate();

    postController.deletePost(postId);
  }

  /**
   * List all entities.
   *
   * @param sorter the sorter
   * @param category the category
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @param user the user
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.list",
      path = "posts",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Post> list(
      @Nullable @Named("accountId") String accountId,
      @Nullable @Named("sort") PostSorter sorter,
      @Nullable @Named("category") String category,
      @Nullable @Named("tags") String tags,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return postController.listPosts(
        Optional.fromNullable(accountId),
        Optional.fromNullable(sorter),
        Optional.fromNullable(category),
        Optional.fromNullable(tags),
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        Optional.absent(),
        user);
  }

  /**
   * Report.
   *
   * @param postId the blog id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.report",
      path = "posts/{postId}/report",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public void report(@Named("postId") String postId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."))
        .validate();

    postController.reportPost(postId, user);
  }
}