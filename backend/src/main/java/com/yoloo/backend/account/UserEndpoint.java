package com.yoloo.backend.account;

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
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(resource = "accounts", clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = Constants.AUDIENCE_ID, authenticators = FirebaseAuthenticator.class)
public class UserEndpoint {

  private final AccountController accountController = AccountControllerProvider.of().create();

  /**
   * Returns the {@link Account} with the corresponding ID.
   *
   * @param userId the ID of the user to be retrieved
   * @param user the user
   * @return the user with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.get", path = "users/{userId}", httpMethod = ApiMethod.HttpMethod.GET)
  public Account get(@Named("userId") String userId, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(userId, "userId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(userId, "Invalid userId."));

    return accountController.getAccount(userId, user);
  }

  /**
   * Register user.
   *
   * @param request the request
   * @return the account
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.register", path = "users", httpMethod = ApiMethod.HttpMethod.POST)
  public Account registerUser(HttpServletRequest request) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(request.getHeader(OAuth2.HeaderType.AUTHORIZATION),
            "Authorization Header cannot be null."));

    return accountController.insertAccount(request);
  }

  /**
   * Check username wrapped boolean.
   *
   * @param username the username
   * @return the wrapped boolean
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.checkUsername",
      path = "users/checkUsername",
      httpMethod = ApiMethod.HttpMethod.GET)
  public WrappedBoolean checkUsername(@Named("username") String username) throws ServiceException {

    EndpointsValidator.create().on(BadRequestValidator.create(username, "username is required."));

    return accountController.checkUsername(username);
  }

  /**
   * Search collection response.
   *
   * @param query the query
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.search", path = "users", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> search(@Named("q") String query,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(query, "query is required."))
        .on(AuthValidator.create(user));

    return accountController.searchAccounts(query, Optional.fromNullable(cursor),
        Optional.fromNullable(limit));
  }

  /**
   * Returns the authenticated {@link Account}.
   *
   * @param user the authenticated user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.me.get", path = "users/me", httpMethod = ApiMethod.HttpMethod.GET)
  public Account getMe(User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return accountController.getAccount(user.getUserId(), user);
  }

  /**
   * Updates an existing authenticated {@code Account}.
   *
   * @param mediaId the media id
   * @param realName the real name
   * @param username the username
   * @param websiteUrl the website url
   * @param bio the bio
   * @param gender the gender
   * @param user the user
   * @return the updated version of the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.me.update", path = "users/me", httpMethod = ApiMethod.HttpMethod.PUT)
  public Account updateMe(@Nullable @Named("mediaId") String mediaId,
      @Nullable @Named("name") String realName, @Nullable @Named("email") String email,
      @Nullable @Named("username") String username,
      @Nullable @Named("websiteUrl") String websiteUrl, @Nullable @Named("bio") String bio,
      @Nullable @Named("gender") Account.Gender gender, User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return accountController.updateAccount(user.getUserId(), Optional.fromNullable(mediaId),
        Optional.fromNullable(username), Optional.fromNullable(realName),
        Optional.fromNullable(email), Optional.fromNullable(websiteUrl), Optional.fromNullable(bio),
        Optional.fromNullable(gender));
  }

  /**
   * Deletes the specified {@code Account}.
   *
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.me.delete", path = "users/me", httpMethod = ApiMethod.HttpMethod.DELETE)
  public void deleteMe(User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    accountController.deleteAccount(user.getUserId());
  }

  /**
   * Register admin user.
   *
   * @return the account
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "admin.users.register",
      path = "admin/users",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Account registerAdmin() throws ServiceException {
    return accountController.insertAdmin();
  }

  /**
   * Deletes the specified {@code Account}.
   *
   * @param userId the user id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "admin.users.delete",
      path = "admin/users/{userId}",
      httpMethod = ApiMethod.HttpMethod.DELETE,
      authenticators = AdminAuthenticator.class)
  public void delete(@Named("userId") String userId, User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    accountController.deleteAccount(userId);
  }
}