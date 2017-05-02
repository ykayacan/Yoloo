package com.yoloo.backend.country;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.country.json.GeognosResponse;
import com.yoloo.backend.util.ServerConfig;
import io.reactivex.Single;
import ix.Ix;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;
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
              MediaConfig.SERVE_FLAGS_BUCKET + "/" + countryCode.toLowerCase() + ".png";

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
        }).blockingGet();
  }

  public Map<Key<Country>, Country> getCountries(String... countryCodes) {
    Preconditions.checkNotNull(countryCodes);

    return ofy().load().keys(Ix.fromArray(countryCodes).map(Key::<Country>create).toList());
  }

  public Collection<Country> searchCountry(String q) {
    q = q.toLowerCase().trim();

    Query<Country> query = ofy()
        .load()
        .type(Country.class)
        .filter(Country.FIELD_NAME + " >=", q)
        .filter(Country.FIELD_NAME + " <", q + "\ufffd");

    query = query.limit(15);

    return query.list();
  }
}
