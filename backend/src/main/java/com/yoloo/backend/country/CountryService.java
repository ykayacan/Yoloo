package com.yoloo.backend.country;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.users.User;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.country.json.GeognosResponse;
import com.yoloo.backend.util.ServerConfig;
import io.reactivex.Single;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import static com.yoloo.backend.OfyService.ofy;

@AllArgsConstructor
public class CountryService {

  private static final String GEOGNOS_ENDPOINT = "http://www.geognos.com/api/en/countries/info";

  private URLFetchService urlFetchService;
  private ImagesService imagesService;

  public Country getCountry(String countryCode) {
    Preconditions.checkNotNull(countryCode);

    try {
      return ofy().load().type(Country.class).id(countryCode).safe();
    } catch (NotFoundException e) {
      return createCountry(countryCode);
    }
  }

  @SneakyThrows({MalformedURLException.class})
  private Country createCountry(String countryCode) {
    ObjectMapper mapper = new ObjectMapper();

    Future<HTTPResponse> future = urlFetchService.fetchAsync(
        new URL(GEOGNOS_ENDPOINT + "/" + countryCode.toUpperCase() + ".json"));

    return Single
        .fromFuture(future)
        .map(HTTPResponse::getContent)
        .map(bytes -> mapper.readValue(bytes, GeognosResponse.class))
        .map(response -> {
          final String filePath =
              MediaConfig.SERVE_FLAGS + "/" + countryCode.toLowerCase() + ".png";

          String flagIconUrl;
          if (ServerConfig.isDev()) {
            flagIconUrl = filePath;
          } else {
            ServingUrlOptions options =
                ServingUrlOptions.Builder.withGoogleStorageFileName(filePath).secureUrl(true);

            flagIconUrl = imagesService.getServingUrl(options);
          }

          return Country
              .builder()
              .id(countryCode.toLowerCase())
              .name(response.getResults().getName())
              .flagUrl(new Link(flagIconUrl))
              .build();
        })
        .flatMap(country -> {
          ofy().save().entity(country);
          return Single.just(country);
        })
        .blockingGet();
  }

  /**
   * List visited countries collection.
   *
   * @param userId the user id
   * @param user the user
   * @return the collection
   */
  public Collection<Country> listVisitedCountries(@Nonnull String userId, User user) {
    Account account = ofy().load().key(Key.<Account>create(userId)).now();

    if (account.getVisitedCountries() == null) {
      return Collections.emptyList();
    }

    return account.getVisitedCountries();
  }

  public void deleteVisitedCountry(@Nonnull String countryCode, User user) {
    Account account = ofy().load().key(Key.<Account>create(user.getUserId())).now();

    Set<Country> visited = account.getVisitedCountries();

    if (visited != null) {
      Iterator<Country> it = visited.iterator();

      while (it.hasNext()) {
        if (it.next().getId().equals(countryCode)) {
          it.remove();
        }
      }

      account.withVisitedCountries(visited);

      ofy().save().entity(account);
    }
  }
}
