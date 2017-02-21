package com.yoloo.backend.device;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;
import com.yoloo.backend.Constants;
import com.yoloo.backend.authentication.authenticators.FirebaseAuthenticator;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import java.util.logging.Logger;
import javax.inject.Named;

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
    resource = "devices",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID},
    audiences = {Constants.AUDIENCE_ID,},
    authenticators = {
        FirebaseAuthenticator.class
    }
)
public class DeviceEndpoint {

  private static final Logger LOG =
      Logger.getLogger(DeviceEndpoint.class.getName());

  /**
   * Register device.
   *
   * @param regId the reg id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "devices.register",
      path = "devices",
      httpMethod = ApiMethod.HttpMethod.POST)
  public void registerDevice(@Named("regId") String regId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    getDeviceController().registerDevice(regId, user);
  }

  /**
   * Unregister device.
   *
   * @param regId the reg id
   * @param user the user
   * @throws ServiceException the service exception
   */
  @ApiMethod(
      name = "devices.unregister",
      path = "devices",
      httpMethod = ApiMethod.HttpMethod.DELETE)
  public void unregisterDevice(@Named("regId") String regId, User user) throws ServiceException {

    EndpointsValidator.create()
        .on(AuthValidator.create(user))
        .validate();

    getDeviceController().unregisterDevice(regId, user);
  }

  private DeviceController getDeviceController() {
    return DeviceController.create();
  }
}
