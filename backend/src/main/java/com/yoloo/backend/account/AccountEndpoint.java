package com.yoloo.backend.account;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;

import java.util.concurrent.ExecutionException;
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
final class AccountEndpoint {

    private static final Logger logger =
            Logger.getLogger(AccountEndpoint.class.getSimpleName());

    /**
     * Returns the {@link Account} with the corresponding ID.
     *
     * @param websafeAccountId the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "accounts.get",
            path = "accounts/{accountId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Account get(@Named("accountId") String websafeAccountId,
                       HttpServletRequest request,
                       User user)
            throws ServiceException {
        Validator.builder()
                .addRule(new IdValidationRule(websafeAccountId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeAccountId))
                .validate();

        return null;
    }

    @ApiMethod(
            name = "accounts.register",
            path = "accounts",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Account register(@Named("locale") String locale,
                            HttpServletRequest request)
            throws ServiceException {
        String idToken = request.getHeader(OAuth2.HeaderType.AUTHORIZATION).split(" ")[1];

        Task<FirebaseToken> authTask = FirebaseAuth.getInstance().verifyIdToken(idToken)
                .addOnSuccessListener(new OnSuccessListener<FirebaseToken>() {
                    @Override
                    public void onSuccess(FirebaseToken decodedToken) {
                    }
                });

        try {
            Tasks.await(authTask);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        FirebaseToken token = authTask.getResult();

        return getAccountController().add(token);
    }

    /**
     * Updates an existing {@code Account}.
     *
     * @param user the user
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "accounts.update",
            path = "accounts/{accountId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Account update(@Named("accountId") String websafeAccountId,
                          @Nullable @Named("mediaId") String mediaId,
                          @Nullable @Named("badgeName") String badgeName,
                          HttpServletRequest request,
                          User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return null;
    }

    /**
     * Deletes the specified {@code Account}.
     *
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "accounts.delete",
            path = "accounts/{accountId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("accountId") String websafeAccountId,
                       HttpServletRequest request,
                       User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        // TODO: 7.07.2016 Implement parentUserKey delete.
    }

    /**
     * Returns the {@link Account}.
     *
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "accounts.followers",
            path = "accounts/{accountId}/followers",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> followers(@Named("accountId") String websafeAccountId,
                                                 @Nullable @Named("cursor") String cursor,
                                                 @Nullable @Named("limit") Integer limit,
                                                 HttpServletRequest request,
                                                 User user)
            throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return null;
    }

    /**
     * Returns the {@link Account}.
     *
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "accounts.followings",
            path = "accounts/{accountId}/followings",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> followings(@Named("accountId") String websafeAccountId,
                                                  @Nullable @Named("cursor") final String cursor,
                                                  @Nullable @Named("limit") Integer limit,
                                                  HttpServletRequest request,
                                                  User user)
            throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return null;
    }

    private AccountController getAccountController() {
        return AccountController.newInstance(
                AccountService.newInstance(),
                AccountShardService.newInstance(),
                GamificationService.newInstance()
        );
    }
}