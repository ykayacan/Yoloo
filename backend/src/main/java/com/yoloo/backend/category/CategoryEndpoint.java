package com.yoloo.backend.category;

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
import com.yoloo.backend.category.sort_strategy.CategorySorter;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;

import java.util.Collection;

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
    resource = "categories",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID }
)
public class CategoryEndpoint {

  private final CategoryController categoryController = CategoryControllerFactory.of().create();

  /**
   * Inserts a new {@code Category}.
   *
   * @param displayName the name
   * @param imageName the image name
   * @param user the user
   * @return the category
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.insert",
      path = "categories",
      httpMethod = ApiMethod.HttpMethod.POST,
      authenticators = AdminAuthenticator.class)
  public Category insert(
      @Named("displayName") String displayName,
      @Named("imageName") String imageName,
      User user)
      throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(displayName, "displayName is required."))
        .on(BadRequestValidator.create(imageName, "imageName is required."))
        .on(AuthValidator.create(user));

    return categoryController.insertCategory(displayName, imageName);
  }

  /**
   * Updates an existing {@code Category}.
   *
   * @param categoryId the websafe category id
   * @param displayName the name
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.update",
      path = "categories/{categoryId}",
      httpMethod = ApiMethod.HttpMethod.PUT,
      authenticators = AdminAuthenticator.class)
  public Category update(
      @Named("categoryId") String categoryId,
      @Nullable @Named("displayName") String displayName,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(categoryId, "categoryId is required."))
        .on(AuthValidator.create(user));

    return categoryController.updateCategory(categoryId, Optional.fromNullable(displayName));
  }

  /**
   * List all entities.
   *
   * @param sorter the sorter
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.list",
      path = "categories",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Category> list(
      @Nullable @Named("categoryIds") String categoryIds,
      @Nullable @Named("sort") CategorySorter sorter,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit)
      throws ServiceException {

    return categoryController.listCategories(
        Optional.fromNullable(categoryIds),
        Optional.fromNullable(sorter),
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }

  /**
   * List interested categories collection.
   *
   * @param userId the user id
   * @param user the user
   * @return the collection
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "categories.interestedCategories",
      path = "categories/{userId}/interestedCategories",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = FirebaseAuthenticator.class)
  public Collection<Category> listInterestedCategories(@Named("userId") String userId, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return categoryController.listInterestedCategories(userId);
  }
}
