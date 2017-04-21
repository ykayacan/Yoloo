package com.yoloo.backend.travelertype;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.AdminAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import com.yoloo.backend.endpointsvalidator.validator.TravelerTypeConflictValidator;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(resource = "travelerTypes", clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = {Constants.AUDIENCE_ID})
public class TravelerTypeEndpoint {

  private final TravelerTypeController controller = TravelerTypeControllerFactory.of().create();

  /**
   * Insert traveler type entity.
   *
   * @param displayName the display name
   * @param imageName the image name
   * @param groupIds the group ids
   * @param user the user
   * @return the traveler type entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "travelerTypes.insert",
      path = "travelerTypes",
      httpMethod = ApiMethod.HttpMethod.POST,
      authenticators = AdminAuthenticator.class)
  public TravelerTypeEntity insert(@Named("displayName") String displayName,
      @Named("imageName") String imageName, @Named("groupIds") String groupIds, User user)
      throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(displayName, "displayName is required."))
        .on(BadRequestValidator.create(imageName, "imageName is required."))
        .on(BadRequestValidator.create(groupIds, "groupIds is required."))
        .on(AuthValidator.create(user))
        .on(TravelerTypeConflictValidator.create(displayName));

    return controller.insertTravelerType(displayName, imageName, groupIds);
  }

  /**
   * Update traveler type entity.
   *
   * @param travelerTypeId the traveler type id
   * @param displayName the display name
   * @param imageName the image name
   * @param user the user
   * @return the traveler type entity
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "travelerTypes.update",
      path = "travelerTypes/{travelerTypeId}",
      httpMethod = ApiMethod.HttpMethod.PUT,
      authenticators = AdminAuthenticator.class)
  public TravelerTypeEntity update(@Named("travelerTypeId") String travelerTypeId,
      @Nullable @Named("displayName") String displayName,
      @Nullable @Named("imageName") String imageName, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(travelerTypeId, "travelerTypeId is required."))
        .on(AuthValidator.create(user));

    return controller.updateTravelerType(travelerTypeId, displayName, imageName);
  }

  /**
   * List list.
   *
   * @return the list
   */
  @ApiMethod(name = "travelerTypes.list",
      path = "travelerTypes",
      httpMethod = ApiMethod.HttpMethod.GET)
  public List<TravelerTypeEntity> list() {
    return controller.listTravelerTypes();
  }
}
