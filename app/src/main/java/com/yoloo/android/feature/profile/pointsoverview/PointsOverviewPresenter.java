package com.yoloo.android.feature.profile.pointsoverview;

import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class PointsOverviewPresenter extends MvpPresenter<PointsOverviewView> {

  private final UserRepository userRepository;

  PointsOverviewPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void onAttachView(PointsOverviewView view) {
    super.onAttachView(view);
    loadGameInfo();
  }

  private void loadGameInfo() {
    Disposable d = userRepository
        .getGameInfo()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(gameInfo -> getView().onLoaded(gameInfo),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
