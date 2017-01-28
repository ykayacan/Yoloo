package com.yoloo.backend.blog;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.Constants;
import com.yoloo.backend.account.AccountService;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.comment.CommentService;
import com.yoloo.backend.comment.CommentShardService;
import com.yoloo.backend.gamification.GamificationService;
import com.yoloo.backend.media.MediaService;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.tag.TagShardService;
import com.yoloo.backend.topic.CategoryShardService;
import com.yoloo.backend.validator.Validator;
import com.yoloo.backend.validator.rule.common.AuthValidator;
import com.yoloo.backend.validator.rule.common.BadRequestValidator;
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
    resource = "blogs",
    clientIds = {
        Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
    },
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {
        FirebaseAuthenticator.class
    })
public class BlogEndpoint {

  /**
   * Add blog.
   *
   * @param title the title
   * @param content the content
   * @param tags the tags
   * @param categories the categories
   * @param mediaId the media id
   * @param user the user
   * @return the blog
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "blogs.add",
      path = "blogs",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Blog add(@Named("title") String title, @Named("content") String content,
      @Named("tags") String tags, @Named("categories") String categories,
      @Nullable @Named("mediaId") String mediaId, User user) throws ServiceException {

    Validator.builder()
        .addRule(new BadRequestValidator(title, tags, content, categories))
        .addRule(new AuthValidator(user))
        .validate();

    return getBlogController().add(title, content, tags, categories, Optional.fromNullable(mediaId),
        user);
  }

  private BlogController getBlogController() {
    return BlogController.create(
        BlogService.create(),
        CommentService.create(CommentShardService.create()),
        CommentShardService.create(),
        TagShardService.create(),
        CategoryShardService.create(),
        AccountService.create(),
        AccountShardService.create(),
        GamificationService.create(),
        MediaService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()));
  }
}
