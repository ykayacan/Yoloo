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
import com.yoloo.backend.follow.FollowController;
import com.yoloo.backend.follow.FollowService;
import com.yoloo.backend.follow.ListType;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;
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

  private static final Logger logger =
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

    Validator.builder()
        .addRule(new IdValidationRule(accountId))
        .addRule(new AuthValidator(user))
        .addRule(new NotFoundRule(accountId))
        .validate();

    return getAccountController().get(accountId, user);
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
  public Account register(@Named("locale") String locale, @Named("gender") Account.Gender gender,
      @Named("topicIds") String topicIds, HttpServletRequest request) throws ServiceException {

    return getAccountController().add(locale, gender, topicIds, request);
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
  public Account update(@Named("accountId") String accountId,
      @Nullable @Named("mediaId") String mediaId, @Nullable @Named("username") String username,
      @Nullable @Named("badge") String badge, User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return getAccountController().update(
        accountId,
        Optional.fromNullable(mediaId),
        Optional.fromNullable(username),
        user);
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

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    getAccountController().delete(user);
  }

  @ApiMethod(
      name = "accounts.list",
      path = "accounts",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> list(@Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit, User user) throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return null;
  }

  /**
   * Check username is available.
   *
   * @param username the username
   * @return the wrapper boolean
   * @throws ServiceException the service exception
   */
  public WrapperBoolean checkUsername(@Named("username") String username) throws ServiceException {
    return getAccountController().checkUsername(username);
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
  public CollectionResponse<Account> followers(@Named("accountId") String accountId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return getFollowController().list(
        accountId,
        ListType.FOLLOWER,
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        user);
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
  public CollectionResponse<Account> followings(@Named("accountId") String accountId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    Validator.builder()
        .addRule(new AuthValidator(user))
        .validate();

    return getFollowController().list(
        accountId,
        ListType.FOLLOWING,
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        user);
  }

  private AccountController getAccountController() {
    return AccountController.create(
        AccountService.create(),
        AccountShardService.create(),
        GamificationService.create()
    );
  }

  private FollowController getFollowController() {
    return FollowController.create(
        FollowService.create(),
        AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService())
    );
  }
}