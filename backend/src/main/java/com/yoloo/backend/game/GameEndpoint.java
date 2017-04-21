package com.yoloo.backend.game;

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

@Api(name = "yolooApi",
     version = "v1",
     namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(resource = "accounts", clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = Constants.AUDIENCE_ID, authenticators = FirebaseAuthenticator.class)
public class GameEndpoint {

  /**
   * Gets game info.
   *
   * @param user the user
   * @return the game info
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "users.me.getGameInfo",
             path = "users/me/gameInfo",
             httpMethod = ApiMethod.HttpMethod.GET) public GameInfo getGameInfo(User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return getGameService().getGameInfo(user);
  }

  private GameService getGameService() {
    return GameService.create();
  }
}
