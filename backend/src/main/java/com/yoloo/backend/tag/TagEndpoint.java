package com.yoloo.backend.tag;

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

/**
 * The type Tag endpoint.
 */
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
        resource = "tags",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                FirebaseAuthenticator.class
        }
)
final class TagEndpoint {

    private static final Logger logger =
            Logger.getLogger(TagEndpoint.class.getSimpleName());

    /**
     * Inserts a new {@code Tag}.
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
            name = "tags.add",
            path = "tags",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Tag addTag(@Named("name") String name,
                      @Named("languageCode") String languageCode,
                      @Named("groupIds") String groupIds,
                      HttpServletRequest request,
                      User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController().addTag(name, languageCode, groupIds, user);
    }

    /**
     * Updates an existing {@code Tag}.
     *
     * @param websafeTagId the websafe hash tag id
     * @param name         the name
     * @param request      the desired state from the entity
     * @param user         the user
     * @return the updated version from the entity
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "tags.update",
            path = "tags/{tagId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Tag updateTag(@Named("tagId") String websafeTagId,
                         @Nullable @Named("name") String name,
                         HttpServletRequest request,
                         User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController()
                .updateTag(websafeTagId, Optional.fromNullable(name), user);
    }

    /**
     * Deletes the specified {@code Tag}.
     *
     * @param websafeTagId the websafe hash tag id
     * @param user         the user
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "tags.delete",
            path = "tags/{tagId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void deleteTag(@Named("tagId") String websafeTagId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeTagId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeTagId))
                .addRule(new AllowedToOperate(user, websafeTagId, AllowedToOperate.Operation.DELETE))
                .validate();

        getHashTagController().deleteTag(websafeTagId, user);
    }

    /**
     * List all {@code Tag} entities.
     *
     * @param name  the name
     * @param limit the maximum number of entries to return
     * @param user  the user
     * @return a response that encapsulates the result list and the next page token/cursor
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "tags.list",
            path = "tags",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Tag> list(@Named("name") String name,
                                        @Nullable @Named("limit") Integer limit,
                                        User user) throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController().list(
                name,
                Optional.fromNullable(limit),
                user);
    }

    /**
     * Inserts a new {@code TagGroup}.
     *
     * @param name    the name
     * @param request the request
     * @param user    the user
     * @return the comment
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "tagGroups.add",
            path = "tagGroups",
            httpMethod = ApiMethod.HttpMethod.POST)
    public TagGroup addGroup(@Named("name") String name,
                             HttpServletRequest request,
                             User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getHashTagController().addGroup(name, user);
    }

    /**
     * Updates an existing {@code TagGroup}.
     *
     * @param websafeGroupId the websafe group id
     * @param name           the name
     * @param request        the desired state from the entity
     * @param user           the user
     * @return the updated version from the entity
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "tagGroups.update",
            path = "tagGroups/{groupId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public TagGroup updateGroup(@Named("groupId") String websafeGroupId,
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
     * Deletes the specified {@code TagGroup}.
     *
     * @param websafeGroupId the websafe group id
     * @param user           the user
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "tagGroups.delete",
            path = "tagGroups/{groupId}",
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

    private TagController getHashTagController() {
        return TagController.create(
                TagService.create(),
                TagShardService.create()
        );
    }
}