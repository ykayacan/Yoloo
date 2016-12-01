package com.yoloo.backend.hashtag;

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
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AllowedToOperate;
import com.yoloo.backend.validator.rule.common.AuthenticationRule;
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
        resource = "hashtags",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                FirebaseAuthenticator.class
        }
)
final class HashTagEndpoint {

    private static final Logger logger =
            Logger.getLogger(HashTagEndpoint.class.getSimpleName());

    /**
     * Inserts a new {@code HashTag}.
     *
     * @param name         the name
     * @param languageCode the language code
     * @param groupIds     the group ids
     * @param request      the request
     * @param user         the user
     * @return the comment
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtags.add",
            path = "hashtags",
            httpMethod = ApiMethod.HttpMethod.POST)
    public HashTag addHashTag(@Named("name") String name,
                              @Named("languageCode") String languageCode,
                              @Named("groupIds") String groupIds,
                              HttpServletRequest request,
                              User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController().addHashTag(name, languageCode, groupIds, user);
    }

    /**
     * Updates an existing {@code HashTag}.
     *
     * @param websafeHashTagId the websafe hash tag id
     * @param name             the name
     * @param request          the desired state from the entity
     * @param user             the user
     * @return the updated version from the entity
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtags.update",
            path = "hashtags/{hashtagId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public HashTag updateHashTag(@Named("hashtagId") String websafeHashTagId,
                                 @Nullable @Named("name") String name,
                                 HttpServletRequest request,
                                 User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController()
                .updateHashTag(websafeHashTagId, Optional.fromNullable(name), user);
    }

    /**
     * Deletes the specified {@code HashTag}.
     *
     * @param websafeHashTagId the websafe hash tag id
     * @param user             the user
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtags.delete",
            path = "hashtags/{hashtagId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void deleteHashTag(@Named("hashtagId") String websafeHashTagId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeHashTagId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeHashTagId))
                .addRule(new AllowedToOperate(user, websafeHashTagId, AllowedToOperate.Operation.DELETE))
                .validate();

        getHashTagController().deleteHashTag(websafeHashTagId, user);
    }

    /**
     * List all {@code Comment} entities.
     *
     * @param websafeQuestionId the websafe question id
     * @param cursor            used for pagination to determine which page to return
     * @param limit             the maximum number of entries to return
     * @param user              the user
     * @return a response that encapsulates the result list and the next page token/cursor
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtags.list",
            path = "hashtags",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<HashTag> list(@Named("name") String name,
                                            @Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit,
                                            User user) throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController().list(
                name,
                Optional.fromNullable(cursor),
                Optional.fromNullable(limit),
                user);
    }

    /**
     * Inserts a new {@code HashTagGroup}.
     *
     * @param name    the name
     * @param request the request
     * @param user    the user
     * @return the comment
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtagGroups.add",
            path = "hashtagGroups",
            httpMethod = ApiMethod.HttpMethod.POST)
    public HashTagGroup addGroup(@Named("name") String name,
                                 HttpServletRequest request,
                                 User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController().addGroup(name, user);
    }

    /**
     * Updates an existing {@code HashTagGroup}.
     *
     * @param websafeGroupId the websafe group id
     * @param name           the name
     * @param request        the desired state from the entity
     * @param user           the user
     * @return the updated version from the entity
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtagGroups.update",
            path = "hashtagGroups/{groupId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public HashTagGroup updateGroup(@Named("groupId") String websafeGroupId,
                                    @Nullable @Named("name") String name,
                                    HttpServletRequest request,
                                    User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController()
                .updateGroup(websafeGroupId, Optional.fromNullable(name), user);
    }

    /**
     * Deletes the specified {@code HashTagGroup}.
     *
     * @param websafeGroupId the websafe group id
     * @param user           the user
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "hashtagGroups.delete",
            path = "hashtagGroups/{groupId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void deleteGroup(@Named("groupId") String websafeGroupId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeGroupId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeGroupId))
                .addRule(new AllowedToOperate(user, websafeGroupId, AllowedToOperate.Operation.DELETE))
                .validate();

        getHashTagController().deleteGroup(websafeGroupId, user);
    }

    private HashTagController getHashTagController() {
        return HashTagController.newInstance(
                HashTagService.newInstance(),
                HashTagShardService.newInstance()
        );
    }
}