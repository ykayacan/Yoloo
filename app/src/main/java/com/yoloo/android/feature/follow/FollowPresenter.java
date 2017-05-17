package com.yoloo.android.feature.follow;

import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class FollowPresenter extends MvpPresenter<FollowView> {

  private final UserRepository userRepository;

  private String cursor;

  FollowPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  void loadFollowers(boolean pullToRefresh, boolean loadingMore, @Nonnull String userId) {
    resetCursor(pullToRefresh);

    Disposable d = userRepository
        .listFollowers(userId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(response -> {
          cursor = response.getCursor();

          if (loadingMore) {
            getView().onLoadedMore(response.getData());
          } else {
            getView().onLoaded(response.getData());
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadFollowings(boolean pullToRefresh, boolean loadingMore, @Nonnull String userId) {
    resetCursor(pullToRefresh);

    Disposable d = userRepository
        .listFollowings(userId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(disposable -> getView().onLoading(loadingMore))
        .subscribe(response -> {
          cursor = response.getCursor();

          if (loadingMore) {
            getView().onLoadedMore(response.getData());
          } else {
            getView().onLoaded(response.getData());
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void follow(@Nonnull String userId, int direction) {
    Disposable d = userRepository
        .relationship(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void resetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }
}
