package com.yoloo.backend.checkin;

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
import com.yoloo.backend.endpointsvalidator.validator.BadRequestValidator;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = Constants.API_OWNER,
        ownerName = Constants.API_OWNER
    ))
@ApiClass(resource = "checkins",
    clientIds = {
        Constants.ANDROID_CLIENT_ID,
        Constants.IOS_CLIENT_ID,
        Constants.WEB_CLIENT_ID
    },
    audiences = {Constants.AUDIENCE_ID},
    authenticators = {FirebaseAuthenticator.class})
public class CheckInEndpoint {

  @ApiMethod(name = "checkins.insert", path = "checkins", httpMethod = ApiMethod.HttpMethod.POST)
  public CheckIn insert(@Named("ll") String location, User user) throws ServiceException {

    EndpointsValidator
        .create()
        .on(BadRequestValidator.create(location, "ll is required."))
        .on(AuthValidator.create(user));

    return getCheckInController().insertCheckIn(location, user);
  }

  private CheckInController getCheckInController() {
    return CheckInController.create();
  }
}
