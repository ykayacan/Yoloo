package com.yoloo.backend.vote;

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
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import com.yoloo.backend.post.PostEntity;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER))
@ApiClass(resource = "votes",
    clientIds = {
        Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID },
    authenticators = { FirebaseAuthenticator.class })
public class VoteEndpoint {

  private final VoteController voteController = VoteControllerFactory.of().create();

  /**
   * Vote {@code Post} with given ID.
   *
   * @param postId the websafe votable id
   * @param direction the direction
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "posts.vote",
      path = "posts/{postId}/votes",
      httpMethod = ApiMethod.HttpMethod.POST)
  public PostEntity votePost(@Named("postId") String postId, @Named("dir") int direction, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."));

    return voteController.votePost(postId, direction, user);
  }

  /**
   * Vote {@code Comment} with given ID.
   *
   * @param commentId the websafe votable id
   * @param direction the direction
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "comments.vote",
      path = "comments/{commentId}/votes",
      httpMethod = ApiMethod.HttpMethod.POST)
  public Comment voteComment(@Named("commentId") String commentId, @Named("dir") int direction,
      User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(commentId, "commentId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(commentId, "Invalid commentId."));

    return voteController.voteComment(commentId, direction, user);
  }

  /**
   * List voters collection response.
   *
   * @param postId the post id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the collection response
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "posts.listVoters",
      path = "posts/{postId}/listVoters",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> listVoters(@Named("postId") String postId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(postId, "postId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(postId, "Invalid postId."));

    return voteController.listVoters(postId, Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }
}
