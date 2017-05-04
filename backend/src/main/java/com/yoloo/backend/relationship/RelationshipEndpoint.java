package com.yoloo.backend.relationship;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.yoloo.backend.Constants;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.account.AccountShardService;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.FollowConflictValidator;
import com.yoloo.backend.endpointsvalidator.validator.FollowValidator;
import com.yoloo.backend.endpointsvalidator.validator.NotFoundValidator;
import com.yoloo.backend.notification.NotificationService;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = {Constants.AUDIENCE_ID}, authenticators = {FirebaseAuthenticator.class})
public class RelationshipEndpoint {

  private final RelationshipController relationshipController =
      RelationshipControllerFactory.of().create();

  /*@ApiMethod(name = "users.relationship.facebook",
      path = "users/{userId}/relationship/facebook",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void relationshipFacebook(@Named("facebookId") String facebookId, User user)
      throws ServiceException {
    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(facebookId, "facebookId is required."))
        .on(AuthValidator.create(user));

    relationshipController.followByFacebookId(facebookId, user);
  }*/

  /**
   * Follows a new {@code Account}.
   *
   * @param userId the websafe user id
   * @param action the action
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.relationship",
      path = "users/{userId}/relationship",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void relationship(@Named("userId") String userId,
      @Named("action") RelationshipAction action, User user) throws ServiceException {

    EndpointsValidator validator = EndpointsValidator
        .create()
        .on(BadRequestValidator.create(userId, "userId is required."))
        .on(BadRequestValidator.create(action, "action is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(userId, "Invalid userId."))
        .on(FollowValidator.create(user.getUserId(), userId));

    switch (action) {
      case FOLLOW:
        validator.on(
            FollowConflictValidator.create(user.getUserId(), userId, "Already following."));

        relationshipController.follow(userId, user);
        break;
      case UNFOLLOW:
        relationshipController.unfollow(userId, user);
        break;
      case APPROVE:
        throw new UnsupportedOperationException("Action is not supported for now.");
      case IGNORE:
        throw new UnsupportedOperationException("Action is not supported for now.");
    }
  }

  /**
   * Returns the {@link Account}.
   *
   * @param userId the websafe account id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.followedBy",
      path = "users/{userId}/followedBy",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> followedBy(@Named("userId") String userId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(userId, "userId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(userId, "Invalid userId."));

    return getRelationshipController().list(userId, RelationshipType.FOLLOWER,
        Optional.fromNullable(limit), Optional.fromNullable(cursor));
  }

  /**
   * Returns the {@link Account}.
   *
   * @param userId the user id
   * @param cursor the cursor
   * @param limit the limit
   * @param user the user
   * @return the entity with the corresponding ID
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.follows",
      path = "users/{userId}/follows",
      httpMethod = ApiMethod.HttpMethod.GET)
  public CollectionResponse<Account> follows(@Named("userId") String userId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(userId, "userId is required."))
        .on(AuthValidator.create(user))
        .on(NotFoundValidator.create(userId, "Invalid userId."));

    return getRelationshipController().list(userId, RelationshipType.FOLLOWING,
        Optional.fromNullable(limit), Optional.fromNullable(cursor));
  }

  private RelationshipController getRelationshipController() {
    return RelationshipController.create(AccountShardService.create(),
        NotificationService.create(URLFetchServiceFactory.getURLFetchService()));
  }
}
