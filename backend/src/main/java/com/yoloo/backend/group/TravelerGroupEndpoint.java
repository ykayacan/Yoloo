package com.yoloo.backend.group;

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
import com.yoloo.backend.authentication.authenticators.AdminAuthenticator;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.GroupConflictValidator;
import com.yoloo.backend.group.sorter.GroupSorter;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(
    name = "yolooApi",
    version = "v1",
    namespace =
    @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER
    )
)
@ApiClass(
    resource = "groups",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = { Constants.AUDIENCE_ID }
)
public class TravelerGroupEndpoint {

  private final TravelerGroupController travelerGroupController =
      TravelerGroupControllerFactory.of().create();

  /**
   * Get traveler group entity.
   *
   * @param groupId the group id
   * @param user the user
   * @return the traveler group entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.get",
      path = "groups/{groupId}",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = FirebaseAuthenticator.class)
  public TravelerGroupEntity get(@Named("groupId") String groupId, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(groupId, "groupId is required."))
        .on(AuthValidator.create(user));

    return travelerGroupController.getGroup(groupId, user);
  }

  /**
   * Inserts a new {@code TravelerGroupEntity}.
   *
   * @param displayName the name
   * @param imageName the image name
   * @param user the user
   * @return the category
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.insert",
      path = "groups",
      httpMethod = ApiMethod.HttpMethod.POST,
      authenticators = AdminAuthenticator.class)
  public TravelerGroupEntity insert(@Named("displayName") String displayName,
      @Named("imageName") String imageName, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(displayName, "displayName is required."))
        .on(BadRequestValidator.create(imageName, "imageName is required."))
        .on(AuthValidator.create(user))
        .on(GroupConflictValidator.create(displayName));

    return travelerGroupController.insertGroup(displayName, imageName);
  }

  /**
   * Update traveler group.
   *
   * @param groupId the group id
   * @param displayName the display name
   * @param user the user
   * @return the traveler group
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.update",
      path = "groups/{groupId}",
      httpMethod = ApiMethod.HttpMethod.PUT,
      authenticators = AdminAuthenticator.class)
  public TravelerGroupEntity update(@Named("groupId") String groupId,
      @Nullable @Named("displayName") String displayName, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(groupId, "groupId is required."))
        .on(AuthValidator.create(user));

    return travelerGroupController.updateGroup(groupId, Optional.fromNullable(displayName));
  }

  @ApiMethod(name = "groups.subscribe",
      path = "groups/{groupId}/subscribe",
      httpMethod = ApiMethod.HttpMethod.POST,
      authenticators = FirebaseAuthenticator.class)
  public TravelerGroupEntity subscribe(@Named("groupId") String groupId, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return travelerGroupController.subscribe(groupId, user);
  }

  @ApiMethod(name = "groups.unsubscribe",
      path = "groups/{groupId}/unsubscribe",
      httpMethod = ApiMethod.HttpMethod.POST,
      authenticators = FirebaseAuthenticator.class)
  public TravelerGroupEntity unsubscribe(@Named("groupId") String groupId, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return travelerGroupController.unsubscribe(groupId, user);
  }

  /**
   * List all entities.
   *
   * @param sorter the sorter
   * @param cursor used for pagination to determine which page to return
   * @param limit the maximum number from entries to return
   * @param user the user
   * @return a response that encapsulates the result listFeed and the next page token/cursor
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.list",
      path = "groups",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = { AdminAuthenticator.class, FirebaseAuthenticator.class })
  public CollectionResponse<TravelerGroupEntity> list(@Nullable @Named("sort") GroupSorter sorter,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return travelerGroupController.listGroups(Optional.fromNullable(sorter),
        Optional.fromNullable(limit), Optional.fromNullable(cursor), user);
  }

  /**
   * List group users collection response.
   *
   * @param groupId the group id
   * @param cursor the cursor
   * @param limit the limit
   * @return the collection response
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.users",
      path = "groups/{groupId}/users",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = FirebaseAuthenticator.class)
  public CollectionResponse<Account> listGroupUsers(@Named("groupId") String groupId,
      @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit)
      throws ServiceException {

    return travelerGroupController.listGroupUsers(groupId, Optional.fromNullable(limit),
        Optional.fromNullable(cursor));
  }

  /**
   * List interested categories collection.
   *
   * @param userId the user id
   * @param user the user
   * @return the collection
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.subscribedGroups",
      path = "groups/{userId}/listSubscribedGroups",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = FirebaseAuthenticator.class)
  public Collection<TravelerGroupEntity> listSubscribedGroups(@Named("userId") String userId,
      User user) throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return travelerGroupController.listSubscribedGroups(userId);
  }

  /**
   * Search groups collection.
   *
   * @param query the query
   * @param user the user
   * @return the collection
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "groups.search",
      path = "groups/search",
      httpMethod = ApiMethod.HttpMethod.GET,
      authenticators = { FirebaseAuthenticator.class, AdminAuthenticator.class })
  public Collection<TravelerGroupEntity> searchGroups(@Named("query") String query, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(query, "query is empty"))
        .on(AuthValidator.create(user));

    return travelerGroupController.searchGroups(query);
  }

  @ApiMethod(name = "groups.setup",
      path = "groups/setup",
      httpMethod = ApiMethod.HttpMethod.GET)
  public void setup(@Named("env") String env) throws ServiceException {
    if (env.equals("prod")) {
      travelerGroupController.setup();
    } else {
      travelerGroupController.setupDev();
    }
  }
}
