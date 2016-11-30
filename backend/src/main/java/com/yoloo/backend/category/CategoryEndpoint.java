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
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.category.sort_strategy.CategorySorter;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthenticationRule;

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
        resource = "categories",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                FirebaseAuthenticator.class
        }
)
final class CategoryEndpoint {

    /**
     * Inserts a new {@code Category}.
     */
    @ApiMethod(
            name = "categories.add",
            path = "categories",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Category add(@Named("name") String name,
                        HttpServletRequest request,
                        User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getCategoryController().add(name, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number from entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "categories.list",
            path = "categories",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Category> list(@Nullable @Named("sort") CategorySorter sorter,
                                             @Nullable @Named("cursor") String cursor,
                                             @Nullable @Named("limit") Integer limit,
                                             User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getCategoryController().list(
                Optional.fromNullable(sorter),
                Optional.fromNullable(limit),
                Optional.fromNullable(cursor),
                user);
    }

    private CategoryController getCategoryController() {
        return CategoryController.newInstance(
                CategoryService.newInstance(),
                CategoryShardService.newInstance()
        );
    }
}
