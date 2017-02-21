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
    audiences = {Constants.AUDIENCE_ID}
)
public class CategoryEndpoint {

  private final CategoryController categoryController = CategoryControllerFactory.of().create();

  /**
   * Inserts a new {@code Category}.
   *
   * @param name the name
   * @param type the type
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
      @Named("name") String name,
      @Named("type") Category.Type type,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(name, "name is required."))
        .on(AuthValidator.create(user))
        .validate();

    return categoryController.insertCategory(name, type);
  }

  /**
   * Updates an existing {@code Category}.
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
      httpMethod = ApiMethod.HttpMethod.PUT,
      authenticators = AdminAuthenticator.class)
  public Category update(
      @Named("categoryId") String categoryId,
      @Nullable @Named("name") String name,
      @Nullable @Named("type") Category.Type type,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    return categoryController.updateCategory(
        categoryId,
        Optional.fromNullable(name),
        Optional.fromNullable(type));
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
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = {
          FirebaseAuthenticator.class,
          AdminAuthenticator.class
      })
  public CollectionResponse<Category> list(
      @Nullable @Named("sort") CategorySorter sorter,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit)
      throws ServiceException {

    return categoryController.listCategories(
        Optional.fromNullable(sorter),
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }
}
