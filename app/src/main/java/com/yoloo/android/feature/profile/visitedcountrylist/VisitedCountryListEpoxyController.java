package com.yoloo.android.feature.profile.visitedcountrylist;

import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.CountryRealm;
import java.util.List;

public class VisitedCountryListEpoxyController extends TypedEpoxyController<List<CountryRealm>> {

  private final RequestManager glide;

  public VisitedCountryListEpoxyController(RequestManager glide) {
    this.glide = glide;
  }

  @Override
  protected void buildModels(List<CountryRealm> countries) {
    Stream
        .of(countries)
        .forEach(country -> new CountryGridModel_()
            .id(country.getCode())
            .country(country)
            .glide(glide)
            .addTo(this));
  }
}
