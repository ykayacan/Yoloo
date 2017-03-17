package com.yoloo.backend.account;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
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
import com.yoloo.backend.follow.FollowController;
import com.yoloo.backend.follow.ListType;
import com.yoloo.backend.notification.NotificationService;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER))
@ApiClass(resource = "accounts",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = Constants.AUDIENCE_ID,
    authenticators = FirebaseAuthenticator.class
)
public class AccountEndpoint {

  private static final Logger LOG = Logger.getLogger(AccountEndpoint.class.getSimpleName());

  private final AccountController accountController = AccountControllerProvider.of().create();

  /**
   * Returns the {@link Account}.
   *
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.me",
      path = "accounts/me",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Account getMe(User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user)).validate();

    return accountController.getAccount(user.getUserId(), user);
  }

  /**
   * Returns the {@link Account} with the corresponding ID.
   *
   * @param accountId the ID of the entity to be retrieved
   * @return the entity with the corresponding ID
   * @throws NotFoundException if there is no {@code Account} with the provided ID.
   */
  @ApiMethod(
      name = "accounts.get",
      path = "accounts/{accountId}",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Account get(@Named("accountId") String accountId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .validate();

    return accountController.getAccount(accountId, user);
  }

  /**
   * Register account.
   *
   * @param locale the locale
   * @param request the request
   * @return the account
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.register",
      path = "accounts",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Account register(
      @Named("realname") String realname,
      @Named("locale") String locale,
      @Named("gender") Account.Gender gender,
      @Named("categoryIds") String categoryIds,
      HttpServletRequest request) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(locale, "locale is required."))
        .on(BadRequestValidator.create(gender, "gender is required."))
        .on(BadRequestValidator.create(categoryIds, "categoryIds is required."))
        .validate();

    return accountController.insertAccount(realname, locale, gender, categoryIds, request);
  }

  /**
   * Register admin account.
   *
   * @return the account
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "admin.accounts.insert",
      path = "admin/accounts",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Account registerAdmin() throws ServiceException {
    return accountController.insertAdmin();
  }

  /**
   * Updates an existing {@code Account}.
   *
   * @param accountId the account id
   * @param mediaId the media id
   * @param username the username
   * @param bio the bio
   * @param user the user
   * @return the updated version of the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.update",
      path = "accounts/{accountId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Account update(
      @Named("accountId") String accountId,
      @Nullable @Named("mediaId") String mediaId,
      @Nullable @Named("name") String realName,
      @Nullable @Named("username") String username,
      @Nullable @Named("websiteUrl") String websiteUrl,
      @Nullable @Named("bio") String bio,
      @Nullable @Named("gender") Account.Gender gender,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .on(ForbiddenValidator.create(accountId, user, ForbiddenValidator.Op.UPDATE))
        .validate();

    return accountController.updateAccount(
        accountId,
        Optional.fromNullable(mediaId),
        Optional.fromNullable(username),
        Optional.fromNullable(realName),
        Optional.fromNullable(websiteUrl),
        Optional.fromNullable(bio),
        Optional.fromNullable(gender));
  }

  /**
   * Deletes the specified {@code Account}.
   *
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.delete",
      path = "accounts",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user)).validate();

    accountController.deleteAccount(user);
  }

  /**
   * Deletes the specified {@code Account}.
   *
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "admin.accounts.delete",
      path = "admin/accounts/{accountId}",
      httpMethod = ApiMethod.HttpMethod.DELETE,
      authenticators = AdminAuthenticator.class)
  public void deleteWithAdminRights(@Named("accountId") String accountId, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user)).validate();

    accountController.deleteAccount(accountId);
  }

  @ApiMethod(
      name = "accounts.search",
      path = "accounts",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> search(
      @Named("q") String query,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user)
      throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(query, "query is required."))
        .on(AuthValidator.create(user))
        .validate();

    return accountController.searchAccounts(query, Optional.fromNullable(cursor),
        Optional.fromNullable(limit));
  }

  /**
   * Returns the {@link Account}.
   *
   * @param accountId the websafe account id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.followers",
      path = "accounts/{accountId}/followers",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> followers(
      @Named("accountId") String accountId,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .validate();

    return getFollowController().list(accountId, ListType.FOLLOWER, Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }

  /**
   * Returns the {@link Account}.
   *
   * @param accountId the account id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.followings",
      path = "accounts/{accountId}/followings",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> followings(
      @Named("accountId") String accountId,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .validate();

    return getFollowController().list(accountId, ListType.FOLLOWING, Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }

  private FollowController getFollowController() {
    return FollowController.create(AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()));
  }
}