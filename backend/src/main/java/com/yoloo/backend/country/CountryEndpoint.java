package com.yoloo.backend.country;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.User;
import com.yoloo.backend.Constants;
import com.yoloo.backend.endpointsvalidator.EndpointsValidator;
import com.yoloo.backend.endpointsvalidator.validator.AuthValidator;
import java.util.Collection;
import javax.inject.Named;

@Api(name = "yolooApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = Constants.API_OWNER, ownerName = Constants.API_OWNER))
@ApiClass(resource = "posts", clientIds = {
    Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.WEB_CLIENT_ID
}, audiences = {Constants.AUDIENCE_ID})
public class CountryEndpoint {

  /**
   * Get country.
   *
   * @param code the code
   * @return the country
   * @throws ServiceException the service exception
   */
  @ApiMethod(name = "countries.get",
      path = "countries/{code}",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Country get(@Named("code") String code) throws ServiceException {

    return new CountryService(URLFetchServiceFactory.getURLFetchService(),
        ImagesServiceFactory.getImagesService()).getCountry(code);
  }

  @ApiMethod(name = "users.visited",
      path = "users/{userId}/visited",
      httpMethod = ApiMethod.HttpMethod.GET)
  public Collection<Country> listVisitedCountries(@Named("userId") String userId, User user)
      throws ServiceException {

    EndpointsValidator.create().on(AuthValidator.create(user));

    return new CountryService(URLFetchServiceFactory.getURLFetchService(),
        ImagesServiceFactory.getImagesService()).listVisitedCountries(userId, user);
  }
}
