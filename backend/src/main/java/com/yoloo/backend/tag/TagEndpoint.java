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
import com.yoloo.backend.authentication.authenticators.AdminAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.ForbiddenValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import java.util.List;
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
        AdminAuthenticator.class
    }
)
public class TagEndpoint {

  private final TagController tagController = TagControllerFactory.of().create();

  /**
   * Inserts a new {@code Tag}.
   *
   * @param name the name
   * @param langCode the language code
   * @param groupIds the group ids
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.insert",
      path = "tags",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Tag insertTag(
      @Named("name") String name,
      @Named("langCode") String langCode,
      @Named("groupIds") String groupIds,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return tagController.insertTag(name, langCode, groupIds);
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
  public Tag updateTag(
      @Named("tagId") String tagId,
      @Nullable @Named("name") String name,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return tagController.updateTag(tagId, Optional.fromNullable(name));
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

    EndpointsValidator.create()
        .on(BadRequestValidator.create(tagId, "tagId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(tagId, "Invalid tagId."))
        .on(ForbiddenValidator.create(tagId, user, ForbiddenValidator.Op.DELETE))
        .validate();

    tagController.deleteTag(tagId);
  }

  /**
   * List all {@code Tag} entities.
   *
   * @param name the name
   * @param cursor the cursor
   * @param limit the maximum number of entries to return
   * @param user the user
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.list",
      path = "tags",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Tag> listTags(
      @Named("name") String name,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return tagController.list(name, Optional.fromNullable(cursor), Optional.fromNullable(limit)
    );
  }

  /**
   * Recommended list.
   *
   * @param user the user
   * @return the listFeed
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "tags.recommended",
      path = "tags/recommended",
      httpMethod = ApiMethod.HttpMethod.GET)
  public List<Tag> recommended(User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return tagController.getRecommendedTags();
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
      name = "tagGroups.insert",
      path = "tagGroups",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Tag insertGroup(@Named("name") String name, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return tagController.insertGroup(name);
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
  public Tag updateGroup(
      @Named("groupId") String groupId,
      @Nullable @Named("name") String name,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return tagController.updateGroup(groupId, Optional.fromNullable(name));
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

    EndpointsValidator.create()
        .on(BadRequestValidator.create(groupId, "groupId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(groupId, "Invalid groupId."))
        .on(ForbiddenValidator.create(groupId, user, ForbiddenValidator.Op.DELETE))
        .validate();

    tagController.deleteGroup(groupId);
  }
}