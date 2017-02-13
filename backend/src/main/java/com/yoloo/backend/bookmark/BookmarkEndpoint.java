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
import com.yoloo.backend.post.Post;
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
    resource = "bookmarks",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
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
  /*@ApiMethod(
      name = "posts.bookmarkPost",
      path = "posts/{postId}/bookmark",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void insert(@Named("postId") String postId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(postId))
        .addRule(new AuthValidator(user))
        .validate();

    bookmarkController.insertBookmark(postId, user);
  }*/

  /**
   * Unsave {@code Post} with given ID.
   *
   * @param postId the post id
   * @param user the user
   * @throws ServiceException the service exception
   */
  /*@ApiMethod(
      name = "posts.unbookmarkPost",
      path = "posts/{postId}/bookmark",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("postId") String postId, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(postId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(postId))
        .addRule(new ForbiddenValidator(user, postId, ForbiddenValidator.Operation.DELETE))
        .validate();

    bookmarkController.delete(postId, user);
  }*/

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
  public CollectionResponse<Post> list(
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return bookmarkController.listBookmarks(
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        user);
  }
}
