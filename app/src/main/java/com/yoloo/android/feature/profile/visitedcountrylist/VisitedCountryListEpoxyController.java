package com.yoloo.android.feature.profile.visitedcountrylist;

import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.CountryRealm;
import java.util.ArrayList;
import java.util.List;

class VisitedCountryListEpoxyController extends Typed2EpoxyController<List<CountryRealm>, Void> {

  private final RequestManager glide;

  private List<CountryRealm> countries;

  private CountryGridModel.OnVisitedCountryRemoveRequestListener listener;

  VisitedCountryListEpoxyController(RequestManager glide) {
    this.glide = glide;
    this.countries = new ArrayList<>();
  }

  public void setListener(
      CountryGridModel.OnVisitedCountryRemoveRequestListener listener) {
    this.listener = listener;
  }

  void removeCountry(CountryRealm country) {
    countries.remove(country);
    setData(countries, null);
  }

  @Override public void setData(List<CountryRealm> countries, Void data2) {
    this.countries = countries;
    super.setData(countries, data2);
  }

  @Override
  protected void buildModels(List<CountryRealm> countries, Void aVoid) {
    Stream
        .of(countries)
        .forEach(country -> new CountryGridModel_()
            .id(country.getCode())
            .country(country)
            .onVisitedCountryRemoveRequestListener(listener)
            .glide(glide)
            .addTo(this));
  }
}
