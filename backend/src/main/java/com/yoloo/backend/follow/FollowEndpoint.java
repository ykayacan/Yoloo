package com.yoloo.backend.follow;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;

import com.yoloo.backend.Constants;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;
import com.yoloo.backend.validator.rule.follow.FollowConflictRule;
import com.yoloo.backend.validator.rule.follow.FollowNotFoundRule;

import java.util.logging.Logger;

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
        resource = "questions",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                FirebaseAuthenticator.class
        }
)
final class FollowEndpoint {

    private static final Logger logger =
            Logger.getLogger(FollowEndpoint.class.getSimpleName());

    /**
     * Follows a new {@code Account}.
     *
     * @param websafeAccountId the websafe account id
     * @param user             the user
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "follows.follow",
            path = "accounts/{accountId}/follows",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void follow(@Named("accountId") String websafeAccountId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeAccountId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeAccountId))
                .addRule(new FollowConflictRule(websafeAccountId, user))
                .validate();

        getFollowController().follow(websafeAccountId, user);
    }

    /**
     * Unfollows the specified {@code Account}.
     *
     * @param websafeAccountId the ID of the entity to delete
     * @param user             the user
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "follows.unfollow",
            path = "accounts/{accountId}/follows",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void unfollow(@Named("accountId") String websafeAccountId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeAccountId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeAccountId))
                .addRule(new FollowNotFoundRule(websafeAccountId, user))
                .validate();

        getFollowController().unfollow(websafeAccountId, user);
    }

    /**
     * List all entities.
     *
     * @param websafeAccountId the websafe account id
     * @param type             the type
     * @param cursor           used for pagination to determine which page to return
     * @param limit            the maximum number of entries to return
     * @param user             the user
     * @return a response that encapsulates the result list and the next page token/cursor
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "follows.list",
            path = "users/{userId}/follows",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> list(@Named("accountId") String websafeAccountId,
                                            @Named("type") FollowController.FollowListType type,
                                            @Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit,
                                            User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getFollowController().list(
                websafeAccountId, type,
                Optional.fromNullable(limit),
                Optional.fromNullable(cursor),
                user);
    }

    private FollowController getFollowController() {
        return FollowController.newInstance(
                FollowService.newInstance(),
                AccountShardService.newInstance(),
                NotificationService.newInstance()
        );
    }
}
