package com.yoloo.android.feature.profile.visitedcountrylist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class VisitedCountryListPresenter extends MvpPresenter<VisitedCountryListView> {

  private final UserRepository userRepository;

  VisitedCountryListPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  void loadVisitedCountries(@Nonnull String userId) {
    Disposable d = userRepository
        .listVisitedCountries(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(countries -> getView().onLoaded(countries),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadMyVisitedCountries() {
    Disposable d = userRepository
        .getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .map(AccountRealm::getVisitedCountries)
        .subscribe(countries -> getView().onLoaded(countries),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void addVisitedCountry(@Nonnull String countryCode) {
    Disposable d = userRepository
        .getMe()
        .flatMap(account -> {
          for (CountryRealm country : account.getVisitedCountries()) {
            if (country.getCode().equals(countryCode)) {
              return Observable.error(new Throwable("100"));
            }
          }

          return Observable.just(
              new AccountRealm().addVisitedCountry(new CountryRealm(countryCode)));
        })
        .flatMapSingle(userRepository::updateMe)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(me -> getView().onMeUpdated(me), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
