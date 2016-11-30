package com.yoloo.backend.saved;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;

import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.question.Question;
import com.yoloo.backend.question.QuestionShardService;
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
final class SaveEndpoint {

    private static final Logger logger =
            Logger.getLogger(SaveEndpoint.class.getSimpleName());

    /**
     * Saves a new {@code Question}.
     */
    @ApiMethod(
            name = "questions.save.add",
            path = "questions/{questionId}/save",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void add(@Named("questionId") String websafeQuestionId,
                    HttpServletRequest request, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        getSavedQuestionController().add(websafeQuestionId, user);
    }

    /**
     * Deletes the specified {@code Feed}.
     *
     * @param websafeQuestionId the ID from the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "questions.save.delete",
            path = "questions/{questionId}/save",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void delete(@Named("questionId") String websafeQuestionId,
                       HttpServletRequest request, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .addRule(new AllowedToOperate(user, websafeQuestionId, AllowedToOperate.Operation.DELETE))
                .validate();

        getSavedQuestionController().delete(websafeQuestionId, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number from entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "questions.save.list",
            path = "questions/save",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Question> list(@Nullable @Named("cursor") String cursor,
                                             @Nullable @Named("limit") Integer limit,
                                             User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getSavedQuestionController().list(
                Optional.fromNullable(limit),
                Optional.fromNullable(cursor),
                user);
    }

    private SavedQuestionController getSavedQuestionController() {
        return SavedQuestionController.newInstance(
                SavedQuestionService.newInstance(),
                QuestionShardService.newInstance()
        );
    }
}
