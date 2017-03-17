package com.yoloo.android.feature.follow;

import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class FollowPresenter extends MvpPresenter<FollowView> {

  private final UserRepository userRepository;

  FollowPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  void loadFollowers(boolean pullToRefresh, String userId, String cursor, int limit) {
    Disposable d = userRepository.listFollowers(userId, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(response -> getView().onLoaded(response),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadFollowings(boolean pullToRefresh, String userId, String cursor, int limit) {
    Disposable d = userRepository.listFollowings(userId, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(response -> getView().onLoaded(response),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void follow(String userId, int direction) {
    Disposable d = userRepository.follow(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }
}
