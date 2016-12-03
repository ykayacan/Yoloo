package com.yoloo.backend.question;

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
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.category.CategoryService;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.question.sort_strategy.QuestionSorter;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.tag.HashTagService;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AllowedToOperate;
import com.yoloo.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.backend.validator.rule.common.IdValidationRule;
import com.yoloo.backend.validator.rule.common.NotFoundRule;
import com.yoloo.backend.validator.rule.question.QuestionCreateRule;

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
final class QuestionEndpoint {

    private static final Logger logger =
            Logger.getLogger(QuestionEndpoint.class.getSimpleName());

    /**
     * Returns the {@link Question} with the corresponding ID.
     *
     * @param websafeQuestionId the ID from the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Feed} with the provided ID.
     */
    @ApiMethod(
            name = "questions.get",
            path = "questions/{questionId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Question get(@Named("questionId") String websafeQuestionId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .validate();

        return getForumController().get(websafeQuestionId, user);
    }

    /**
     * Inserts a new {@code Question}.
     */
    @ApiMethod(
            name = "questions.add",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Question add(@Named("hashtags") String hashTags,
                        @Named("content") String content,
                        @Named("categoryIds") String categoryIds,
                        @Nullable @Named("mediaId") String mediaId,
                        @Nullable @Named("bounty") Integer bounty,
                        HttpServletRequest request,
                        User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new QuestionCreateRule(content, hashTags))
                .addRule(new AuthenticationRule(user))
                .validate();

        QuestionWrapper wrapper = QuestionWrapper.builder()
                .content(content)
                .hashTags(hashTags)
                .categoryIds(categoryIds)
                .mediaId(Optional.fromNullable(mediaId).orNull())
                .bounty(Optional.fromNullable(bounty).or(0))
                .request(request)
                .build();

        return getForumController().add(wrapper, user);
    }

    /**
     * Updates an existing {@code Question}.
     *
     * @param websafeQuestionId the ID from the entity to be updated
     * @param request           the desired state from the entity
     * @return the updated version from the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing {@code
     *                           Question}
     */
    @ApiMethod(
            name = "questions.update",
            path = "questions/{questionId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Question update(@Named("questionId") String websafeQuestionId,
                           @Nullable @Named("bounty") Integer bounty,
                           @Nullable @Named("content") String content,
                           @Nullable @Named("hashTags") String hashTags,
                           @Nullable @Named("mediaId") String mediaId,
                           HttpServletRequest request,
                           User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .addRule(new AllowedToOperate(user, websafeQuestionId, AllowedToOperate.Operation.UPDATE))
                .validate();

        QuestionWrapper wrapper = QuestionWrapper.builder()
                .websafeQuestionId(websafeQuestionId)
                .content(content)
                .hashTags(hashTags)
                .mediaId(mediaId)
                .bounty(Optional.fromNullable(bounty).or(0))
                .request(request)
                .build();

        return getForumController().update(wrapper, user);
    }

    /**
     * Deletes the specified {@code Feed}.
     *
     * @param websafeQuestionId the ID from the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Feed}
     */
    @ApiMethod(
            name = "questions.delete",
            path = "questions/{questionId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void delete(@Named("questionId") String websafeQuestionId, User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafeQuestionId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeQuestionId))
                .addRule(new AllowedToOperate(user, websafeQuestionId, AllowedToOperate.Operation.DELETE))
                .validate();

        getForumController().delete(websafeQuestionId, user);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number from entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "questions.list",
            path = "questions",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Question> list(@Nullable @Named("sort") QuestionSorter sorter,
                                             @Nullable @Named("cursor") String cursor,
                                             @Nullable @Named("limit") Integer limit,
                                             User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return getForumController().list(
                Optional.fromNullable(sorter),
                Optional.fromNullable(limit),
                Optional.fromNullable(cursor),
                user);
    }

    private QuestionController getForumController() {
        return QuestionController.newInstance(
                QuestionService.newInstance(),
                QuestionShardService.newInstance(),
                CommentService.newInstance(),
                CommentShardService.newInstance(),
                HashTagService.newInstance(),
                CategoryService.newInstance(),
                AccountService.newInstance(),
                AccountShardService.newInstance(),
                GamificationService.newInstance(),
                MediaService.newInstance(),
                NotificationService.newInstance());
    }
}