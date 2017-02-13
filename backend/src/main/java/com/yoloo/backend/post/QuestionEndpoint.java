package com.yoloo.backend.post;

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
import com.yoloo.backend.post.sort_strategy.PostSorter;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
import com.yoloo.backend.validator.rule.common.BadRequestValidator;
import com.yoloo.backend.validator.rule.common.ForbiddenValidator;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER,
        packagePath = Constants.API_PACKAGE_PATH))
@ApiClass(
    resource = "questions",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    })
public class QuestionEndpoint {

  private static final Logger LOGGER =
      Logger.getLogger(QuestionEndpoint.class.getSimpleName());

  private final PostController postController = PostControllerFactory.of().create();

  /**
   * Returns the {@code Post} with the corresponding ID.
   *
   * @param questionId the ID from the entity to be retrieved
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.get",
      path = "questions/{questionId}",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Post get(@Named("questionId") String questionId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(questionId))
        .addRule(new AuthValidator(user))
        .validate();

    return postController.getPost(questionId, user);
  }

  /**
   * Inserts a new {@code Post}.
   *
   * @param tags the tags
   * @param content the content
   * @param categories the categories
   * @param mediaId the media id
   * @param bounty the bounty
   * @param user the user
   * @return the question
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.insert",
      path = "questions",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Post insert(
      @Named("content") String content,
      @Named("tags") String tags,
      @Named("categories") String categories,
      @Nullable @Named("mediaId") String mediaId,
      @Nullable @Named("bounty") Integer bounty,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(tags, content, categories))
        .addRule(new AuthValidator(user))
        .validate();

    return postController.insertQuestion(
        content,
        tags,
        categories,
        Optional.fromNullable(mediaId),
        Optional.fromNullable(bounty),
        user);
  }

  /**
   * Updates an existing {@code Post}.
   *
   * @param questionId the ID from the entity to be updated
   * @param bounty the bounty
   * @param content the content
   * @param tags the tags
   * @param mediaId the media id
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.update",
      path = "questions/{questionId}",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public Post update(
      @Named("questionId") String questionId,
      @Nullable @Named("bounty") Integer bounty,
      @Nullable @Named("content") String content,
      @Nullable @Named("tags") String tags,
      @Nullable @Named("mediaId") String mediaId,
      User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new ForbiddenValidator(user, questionId, ForbiddenValidator.Operation.UPDATE))
        .validate();

    return postController.updatePost(
        questionId,
        Optional.absent(),
        Optional.fromNullable(content),
        Optional.fromNullable(bounty),
        Optional.fromNullable(tags),
        Optional.fromNullable(mediaId));
  }

  /**
   * Deletes the specified {@code Post}.
   *
   * @param questionId the ID from the entity to deleteComment
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.delete",
      path = "questions/{questionId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("questionId") String questionId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(questionId))
        .addRule(new AuthValidator(user))
        .addRule(new ForbiddenValidator(user, questionId, ForbiddenValidator.Operation.DELETE))
        .validate();

    postController.deletePost(questionId);
  }

  /**
   * List all entities.
   *
   * @param sorter the sorter
   * @param category the category
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @param user the user
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.list",
      path = "questions",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Post> list(
      @Nullable @Named("accountId") String accountId,
      @Nullable @Named("sort") PostSorter sorter,
      @Nullable @Named("category") String category,
      @Nullable @Named("tags") String tags,
      @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit,
      User user) throws ServiceException {

    Validator.builder().addRule(new AuthValidator(user)).validate();

    return postController.listPosts(
        Optional.fromNullable(accountId),
        Optional.fromNullable(sorter),
        Optional.fromNullable(category),
        Optional.fromNullable(tags),
        Optional.fromNullable(limit),
        Optional.fromNullable(cursor),
        Post.PostType.QUESTION,
        user);
  }

  /**
   * Reports the {@code Post} with the corresponding ID.
   *
   * @param questionId the websafe question id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "questions.report",
      path = "questions/{questionId}/report",
      httpMethod = ApiMethod.HttpMethod.PUT)
  public void report(@Named("questionId") String questionId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(questionId))
        .addRule(new AuthValidator(user))
        .validate();

    postController.reportPost(questionId, user);
  }
}