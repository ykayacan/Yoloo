package com.yoloo.backend.bookmark;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.ForbiddenValidator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
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

  private static final Logger logger =
      Logger.getLogger(BookmarkEndpoint.class.getSimpleName());

  private final BookmarkController bookmarkController = BookmarkControllerFactory.of().create();

  /**
   * Saves a new {@code Question}.
   */
  @ApiMethod(
      name = "questions.save.add",
      path = "questions/{questionId}/save",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void add(@Named("questionId") String questionId, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    bookmarkController.add(questionId, user);
  }

  /**
   * Deletes the specified {@code Feed}.
   *
   * @param questionId the ID from the entity to delete
   * @throws NotFoundException if the {@code id} does not correspond to an existing {@code Feed}
   */
  @ApiMethod(
      name = "questions.save.delete",
      path = "questions/{questionId}/save",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("questionId") String questionId, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(questionId))
        .addRule(new ForbiddenValidator(user, questionId, ForbiddenValidator.Operation.DELETE))
        .validate();

    bookmarkController.delete(questionId, user);
  }

  /**
   * List all entities.
   *
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @return a response that encapsulates the result list and the next page token/cursor
   */
  @ApiMethod(
      name = "questions.save.list",
      path = "questions/save",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Question> list(@Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return bookmarkController.list(Optional.fromNullable(limit), Optional.fromNullable(cursor),
        user);
  }
}
