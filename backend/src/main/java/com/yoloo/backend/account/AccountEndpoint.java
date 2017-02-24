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
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.ForbiddenValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import com.yoloo.backend.follow.FollowController;
import com.yoloo.backend.follow.ListType;
import com.yoloo.backend.game.GamificationService;
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
        ownerName = Constants.API_OWNER,
        packagePath = Constants.API_PACKAGE_PATH
    )
)
@ApiClass(
    resource = "accounts",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID,},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class AccountEndpoint {

  private static final Logger LOG =
      Logger.getLogger(AccountEndpoint.class.getSimpleName());

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

    return getAccountController().getAccount(accountId, user);
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
      @Named("locale") String locale,
      @Named("gender") Account.Gender gender,
      @Named("categoryIds") String categoryIds,
      HttpServletRequest request) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(locale, "locale is required."))
        .on(BadRequestValidator.create(gender, "gender is required."))
        .on(BadRequestValidator.create(categoryIds, "categoryIds is required."))
        .validate();

    return getAccountController().insertAccount(locale, gender, categoryIds, request);
  }

  /**
   * Register admin account.
   *
   * @return the account
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "accounts.admin",
      path = "accounts/admin",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Account registerAdmin() throws ServiceException {
    return getAccountController().insertAdmin();
  }

  /**
   * Updates an existing {@code Account}.
   *
   * @param accountId the account id
   * @param mediaId the media id
   * @param username the username
   * @param badge the badge
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
      @Nullable @Named("username") String username,
      @Nullable @Named("badge") String badge,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(accountId, "accountId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(accountId, "Invalid accountId."))
        .on(ForbiddenValidator.create(accountId, user, ForbiddenValidator.Op.UPDATE))
        .validate();

    return getAccountController().updateAccount(
        accountId,
        Optional.fromNullable(mediaId),
        Optional.fromNullable(username));
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

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    getAccountController().deleteAccount(user);
  }

  @ApiMethod(
      name = "accounts.search",
      path = "accounts",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> search(
      @Named("q") String query,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    EndpointsValidator.create()
        .on(BadRequestValidator.create(query, "query is required."))
        .on(AuthValidator.create(user))
        .validate();

    return getAccountController().searchAccounts(
        query,
        Optional.fromNullable(cursor),
        Optional.fromNullable(limit));
  }

  /**
   * Check username is available.
   *
   * @param username the username
   * @return the wrapper boolean
   * @throws ServiceException the service exception
   */
  /*@ApiMethod(
      name = "accounts.checkUsername",
      path = "accounts/check",
      httpMethod = ApiMethod.HttpMethod.GET)
  public WrapperBoolean checkUsername(@Named("username") String username) throws ServiceException {
    return getAccountController().checkUsername(username);
  }*/

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
      name = "accounts.followerCount",
      path = "accounts/{accountId}/followerCount",
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

    return getFollowController().list(
        accountId,
        ListType.FOLLOWER,
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor)
    );
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
      name = "accounts.followingCount",
      path = "accounts/{accountId}/followingCount",
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

    return getFollowController().list(
        accountId,
        ListType.FOLLOWING,
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor)
    );
  }

  private AccountController getAccountController() {
    return AccountController.create(AccountShardService.create(), GamificationService.create());
  }

  private FollowController getFollowController() {
    return FollowController.create(
        AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService())
    );
  }
}