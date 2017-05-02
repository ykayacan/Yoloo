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

  @Override
  public void onAttachView(VisitedCountryListView view) {
    super.onAttachView(view);
    loadVisitedCountries();
  }

  private void loadVisitedCountries() {
    Disposable d = userRepository
        .getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .map(AccountRealm::getVisitedCountries)
        .subscribe(countries -> getView().onLoaded(countries),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void addVisitedCountry(@Nonnull String countryCode) {
    AccountRealm update = new AccountRealm().addVisitedCountry(new CountryRealm(countryCode));

    Disposable d = Observable
        .just(update)
        .flatMapSingle(userRepository::updateMe)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(me -> getView().onMeUpdated(me), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
