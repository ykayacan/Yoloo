package com.yoloo.backend.comment;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;

import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FacebookAuthenticator;
import com.yoloo.backend.authentication.authenticators.GoogleAuthenticator;
import com.yoloo.backend.authentication.authenticators.YolooAuthenticator;
import com.yoloo.backend.question.QuestionService;
import com.yoloo.backend.question.QuestionShardService;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.comment.CommentCreateRule;
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
        resource = "comments",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class
        }
)
final class CommentEndpoint {

    private static final Logger logger =
            Logger.getLogger(CommentEndpoint.class.getSimpleName());

    /**
     * Inserts a new {@code Comment}.
     *
     * @param websafeQuestionId the websafe question id
     * @param content           the content
     * @param request           the request
     * @param user              the user
     * @return the comment
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "questions.comments.add",
            path = "questions/{questionId}/comments",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Comment add(@Named("questionId") String websafeQuestionId,
                       @Named("content") String content,
                       HttpServletRequest request,
                       User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new CommentCreateRule(content))
                .addRule(new NotFoundRule(websafeQuestionId))
                .validate();

        return getCommentController().add(websafeQuestionId, content, user);
    }

    /**
     * Updates an existing {@code Comment}.
     *
     * @param websafeQuestionId the ID from the entity to be updated
     * @param websafeCommentId  the websafe comment id
     * @param content           the content
     * @param accepted          the accepted
     * @param request           the desired state from the entity
     * @param user              the user
     * @return the updated version from the entity
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "questions.comments.update",
            path = "questions/{questionId}/comments/{commentId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Comment update(@Named("questionId") String websafeQuestionId,
                          @Named("commentId") String websafeCommentId,
                          @Nullable @Named("content") String content,
                          @Nullable @Named("accepted") Boolean accepted,
                          HttpServletRequest request,
                          User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .validate();

        return getCommentController().update(
                websafeQuestionId, websafeCommentId, Optional.fromNullable(content),
                Optional.fromNullable(accepted), user);
    }

    /**
     * Deletes the specified {@code Comment}.
     *
     * @param websafeQuestionId the ID from the entity to be updated
     * @param websafeCommentId  the websafe comment id
     * @param request           the desired state from the entity
     * @param user              the user
     * @return the updated version from the entity
     * @throws ServiceException the service exception
     */
    @ApiMethod(
            name = "questions.comments.delete",
            path = "questions/{questionId}/comments/{commentId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("questionId") String websafeQuestionId,
                       @Named("commentId") String websafeCommentId,
                       HttpServletRequest request,
                       User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .validate();

        getCommentController().remove(websafeQuestionId, websafeCommentId, user);
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
            name = "questions.comments.list",
            path = "questions/{questionId}/comments",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Comment> list(@Named("questionId") final String websafeQuestionId,
                                            @Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit,
                                            final User user) throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .validate();

        // TODO: 29.11.2016 Add sorting by upvotes.

        return getCommentController().list(
                websafeQuestionId,
                Optional.fromNullable(cursor),
                Optional.fromNullable(limit),
                user);
    }

    private CommentController getCommentController() {
        return CommentController.newInstance(
                CommentService.newInstance(),
                CommentShardService.newInstance(),
                QuestionService.newInstance(),
                QuestionShardService.newInstance(),
                GamificationService.newInstance(),
                NotificationService.newInstance());
    }
}
