package com.yoloo.backend.country;

import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.yoloo.backend.util.TestBase;
import org.junit.Test;

public class CountryServiceTest extends TestBase {

  @Test
  public void testCountryResponse() throws Exception {
    CountryService countryService = new CountryService(URLFetchServiceFactory.getURLFetchService(),
        ImagesServiceFactory.getImagesService());

    Country country = countryService.getCountry("TR");
  }
}
