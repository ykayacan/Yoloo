package com.yoloo.backend.topic;

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
import com.yoloo.backend.topic.sort_strategy.CategorySorter;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
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
    resource = "categories",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class TopicEndpoint {

  private final TopicController topicController = TopicControllerFactory.of().create();

  /**
   * Inserts a new {@code Topic}.
   *
   * @param name the name
   * @param type the type
   * @param user the user
   * @return the category
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.add",
      path = "categories",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Topic add(@Named("name") String name, @Named("type") Topic.Type type, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return topicController.add(name, type, user);
  }

  /**
   * Updates an existing {@code Topic}.
   *
   * @param categoryId the websafe category id
   * @param name the name
   * @param type the type
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.update",
      path = "categories/{categoryId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Topic update(@Named("categoryId") String categoryId, @Nullable @Named("name") String name,
      @Nullable @Named("type") Topic.Type type, User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return topicController.update(categoryId, Optional.fromNullable(name),
        Optional.fromNullable(type), user);
  }

  /**
   * List all entities.
   *
   * @param sorter the sorter
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @return a response that encapsulates the result list and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.list",
      path = "categories",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Topic> list(@Nullable @Named("sort") CategorySorter sorter,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit)
      throws ServiceException {

    return topicController.list(Optional.fromNullable(sorter), Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }
}
