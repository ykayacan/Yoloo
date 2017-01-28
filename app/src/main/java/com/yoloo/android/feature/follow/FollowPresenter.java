package com.yoloo.android.feature.follow;

import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class FollowPresenter extends MvpPresenter<FollowView> {

  private final UserRepository userRepository;

  public FollowPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void loadFollowers(boolean pullToRefresh, String userId, String cursor, String eTag,
      int limit) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = userRepository.listFollowers(userId, cursor, eTag, limit)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> getView().onLoaded(response),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  public void loadFollowings(boolean pullToRefresh, String userId, String cursor, String eTag,
      int limit) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = userRepository.listFollowings(userId, cursor, eTag, limit)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> getView().onLoaded(response),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  public void follow(String userId, int direction) {
    Disposable d = userRepository.follow(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }
}
