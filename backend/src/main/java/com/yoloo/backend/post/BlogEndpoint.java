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
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.ForbiddenValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import com.yoloo.backend.post.sort_strategy.PostSorter;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace =
    @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER
    ))
@ApiClass(
    resource = "posts",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {FirebaseAuthenticator.class}
)
public class BlogEndpoint {

  private final PostController postController = PostControllerFactory.of().create();

  /**
   * Returns the {@code Post} with the corresponding ID.
   *
   * @param blogId the ID from the entity to be retrieved
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "blogs.get", path = "blogs/{blogId}", httpMethod = ApiMethod.HttpMethod.GET)
  public PostEntity get(@Named("blogId") String blogId, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(blogId, "blogId is required."))
        .on(AuthValidator.create(user));

    return postController.getPost(blogId, user);
  }

  /**
   * Insert post.
   *
   * @param title the title
   * @param content the content
   * @param tags the tags
   * @param groupId the group id
   * @param mediaIds the media ids
   * @param bounty the bounty
   * @param user the user
   * @return the post
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "blogs.insert", path = "blogs", httpMethod = ApiMethod.HttpMethod.POST)
  public PostEntity insert(@Named("title") String title, @Named("content") String content,
      @Named("tags") String tags, @Named("groupId") String groupId,
      @Nullable @Named("mediaIds") String mediaIds, @Nullable @Named("bounty") Integer bounty,
      User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(title, "title is required."))
        .on(BadRequestValidator.create(content, "content is required."))
        .on(BadRequestValidator.create(tags, "tags is required."))
        .on(BadRequestValidator.create(groupId, "groupId is required."))
        .on(AuthValidator.create(user));

    return postController.insertBlogPost(title, content, tags, groupId,
        Optional.fromNullable(mediaIds), Optional.fromNullable(bounty), user);
  }

  /**
   * Updates an existing {@code Post}.
   *
   * @param blogId the ID from the entity to be updated
   * @param title the title
   * @param content the content
   * @param tags the tags
   * @param mediaId the media id
   * @param user the user
   * @return the updated version from the entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "blogs.update", path = "blogs/{blogId}", httpMethod = ApiMethod.HttpMethod.PUT)
  public PostEntity update(@Named("blogId") String blogId, @Nullable @Named("title") String title,
      @Nullable @Named("content") String content, @Nullable @Named("tags") String tags,
      @Nullable @Named("mediaId") String mediaId, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(blogId, "blogId is required."))
        .on(AuthValidator.create(user))
        .on(ForbiddenValidator.create(blogId, user, ForbiddenValidator.Op.UPDATE));

    return postController.updatePost(blogId, Optional.fromNullable(title),
        Optional.fromNullable(content), Optional.absent(), Optional.fromNullable(tags),
        Optional.fromNullable(mediaId));
  }

  /**
   * Deletes the specified {@code Post}.
   *
   * @param blogId the ID from the entity to deleteMedia
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "blogs.delete",
      path = "blogs/{blogId}",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void delete(@Named("blogId") String blogId, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(blogId, "blogId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(blogId, "Invalid blogId."))
        .on(ForbiddenValidator.create(blogId, user, ForbiddenValidator.Op.DELETE));

    postController.deletePost(blogId);
  }

  /**
   * List collection response.
   *
   * @param userId the user id
   * @param sorter the sorter
   * @param groupId the group id
   * @param tags the tags
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "blogs.list", path = "blogs", httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<PostEntity> list(@Nullable @Named("userId") String userId,
      @Nullable @Named("sort") PostSorter sorter, @Nullable @Named("groupId") String groupId,
      @Nullable @Named("tags") String tags, @Nullable @Named("cursor") String cursor,
      @Nullable @Named("limit") Integer limit, User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return postController.listPosts(Optional.fromNullable(userId), Optional.fromNullable(sorter),
        Optional.fromNullable(groupId), Optional.fromNullable(tags), Optional.fromNullable(limit),
        Optional.fromNullable(cursor), Optional.of(PostEntity.Type.BLOG), user);
  }
}