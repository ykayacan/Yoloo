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
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.ForbiddenValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(resource = "tags", clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = {Constants.AUDIENCE_ID})
public class TagEndpoint {

  private final TagController tagController = TagControllerFactory.of().create();

  /**
   * Inserts a new {@code Tag}.
   *
   * @param name the name
   * @param user the user
   * @return the comment
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "tags.insert",
      path = "tags",
      httpMethod = ApiMethod.HttpMethod.POST,
      authenticators = {
          AdminAuthenticator.class, FirebaseAuthenticator.class
      })
  public Tag insert(@Named("name") String name, User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return tagController.insertTag(name);
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
  @ApiMethod(name = "tags.update",
      path = "tags/{tagId}",
      httpMethod = ApiMethod.HttpMethod.PUT,
      authenticators = {AdminAuthenticator.class})
  public Tag update(@Named("tagId") String tagId, @Nullable @Named("name") String name, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return tagController.updateTag(tagId, Optional.fromNullable(name));
  }

  /**
   * Deletes the specified {@code Tag}.
   *
   * @param tagId the websafe hash tag id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "tags.delete",
      path = "tags/{tagId}",
      httpMethod = ApiMethod.HttpMethod.DELETE,
      authenticators = AdminAuthenticator.class)
  public void delete(@Named("tagId") String tagId, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(tagId, "tagId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(tagId, "Invalid tagId."))
        .on(ForbiddenValidator.create(tagId, user, ForbiddenValidator.Op.DELETE));

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
  @ApiMethod(name = "tags.list",
      path = "tags",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = {AdminAuthenticator.class, FirebaseAuthenticator.class})
  public CollectionResponse<Tag> list(@Named("name") String name,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return tagController.list(name, Optional.fromNullable(cursor), Optional.fromNullable(limit));
  }

  @ApiMethod(name = "tags.listUsedTags",
      path = "groups/{groupId}/tags",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = {AdminAuthenticator.class, FirebaseAuthenticator.class})
  public CollectionResponse<WrappedString> listUsedTags(@Named("groupId") String groupId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return tagController.listUsedTags(groupId, Optional.fromNullable(cursor),
        Optional.fromNullable(limit));
  }

  /**
   * Recommended list.
   *
   * @param user the user
   * @return the listFeed
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "tags.recommended",
      path = "tags/recommended",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = {AdminAuthenticator.class, FirebaseAuthenticator.class})
  public List<Tag> recommended(User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return tagController.getRecommendedTags();
  }
}