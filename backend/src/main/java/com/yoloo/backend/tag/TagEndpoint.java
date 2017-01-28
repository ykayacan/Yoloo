package com.yoloo.backend.tag;

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
import com.yoloo.backend.validator.rule.common.ForbiddenValidator;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;
import java.util.List;
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
    resource = "tags",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class TagEndpoint {

  private static final Logger logger =
      Logger.getLogger(TagEndpoint.class.getSimpleName());

  private final TagController tagController = TagControllerFactory.of().create();

  /**
   * Inserts a new {@code Tag}.
   *
   * @param name the name
   * @param languageCode the language code
   * @param groupIds the group ids
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.add",
      path = "tags",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Tag addTag(@Named("name") String name, @Named("languageCode") String languageCode,
      @Named("groupIds") String groupIds, User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return tagController.addTag(name, languageCode, groupIds, user);
  }

  /**
   * Updates an existing {@code Tag}.
   *
   * @param tagId the websafe hash tag id
   * @param name the name
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.update",
      path = "tags/{tagId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Tag updateTag(@Named("tagId") String tagId, @Nullable @Named("name") String name,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return tagController.updateTag(tagId, Optional.fromNullable(name), user);
  }

  /**
   * Deletes the specified {@code Tag}.
   *
   * @param tagId the websafe hash tag id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.delete",
      path = "tags/{tagId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void deleteTag(@Named("tagId") String tagId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(tagId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(tagId))
        .addRule(new ForbiddenValidator(user, tagId, ForbiddenValidator.Operation.DELETE))
        .validate();

    tagController.deleteTag(tagId, user);
  }

  /**
   * List all {@code Tag} entities.
   *
   * @param name the name
   * @param cursor the cursor
   * @param limit the maximum number of entries to return
   * @param user the user
   * @return a response that encapsulates the result list and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.list",
      path = "tags",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Tag> list(@Named("name") String name,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return tagController.list(name, Optional.fromNullable(cursor), Optional.fromNullable(limit),
        user);
  }

  /**
   * Recommended list.
   *
   * @param user the user
   * @return the list
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.recommended",
      path = "tags/recommended",
      httpMethod = ApiMethod.HttpMethod.GET)
  public List<Tag> recommended(User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return tagController.recommendedTags(user);
  }

  /**
   * Inserts a new {@code TagGroup}.
   *
   * @param name the name
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tagGroups.add",
      path = "tagGroups",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Tag addGroup(@Named("name") String name, User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return tagController.addGroup(name, user);
  }

  /**
   * Updates an existing {@code TagGroup}.
   *
   * @param groupId the websafe group id
   * @param name the name
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tagGroups.update",
      path = "tagGroups/{groupId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Tag updateGroup(@Named("groupId") String groupId, @Nullable @Named("name") String name,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return tagController.updateGroup(groupId, Optional.fromNullable(name), user);
  }

  /**
   * Deletes the specified {@code TagGroup}.
   *
   * @param groupId the websafe group id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tagGroups.delete",
      path = "tagGroups/{groupId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void deleteGroup(@Named("groupId") String groupId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new IdValidationRule(groupId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(groupId))
        .addRule(new ForbiddenValidator(user, groupId, ForbiddenValidator.Operation.DELETE))
        .validate();

    tagController.deleteGroup(groupId, user);
  }
}