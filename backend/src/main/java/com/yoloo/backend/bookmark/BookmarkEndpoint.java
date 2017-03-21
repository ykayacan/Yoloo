package com.yoloo.backend.bookmark;

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
import com.yoloo.backend.post.dto.PostDTO;

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
    resource = "posts",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID },
    authenticators = { FirebaseAuthenticator.class }
)
public class BookmarkEndpoint {

  private static final Logger LOG =
      Logger.getLogger(BookmarkEndpoint.class.getSimpleName());

  private final BookmarkController bookmarkController = BookmarkControllerFactory.of().create();

  /**
   * Save a {@code Post}.
   *
   * @param postId the post id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.bookmark",
      path = "posts/{postId}/bookmarks",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void insert(@Named("postId") String postId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."));

    bookmarkController.insertBookmark(postId, user);
  }

  /**
   * Unsave {@code Post} with given ID.
   *
   * @param postId the post id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "posts.unbookmark",
      path = "posts/{postId}/bookmarks",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("postId") String postId, User user)
      throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."))
        .on(ForbiddenValidator.create(postId, user, ForbiddenValidator.Op.DELETE));

    bookmarkController.deleteBookmark(postId, user);
  }

  /**
   * List all entities.
   *
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   */
  @ApiMethod(
      name = "posts.bookmarkPost.list",
      path = "posts/bookmarks",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<PostDTO> list(
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return bookmarkController.listBookmarks(
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        user);
  }
}
