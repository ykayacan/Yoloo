package com.yoloo.android.feature.profile;

import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class ProfilePresenter extends MvpPresenter<ProfileView> {

  private final UserRepository userRepository;

  ProfilePresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  void loadUserProfile(@Nonnull String userId) {
    Disposable d = userRepository.getUser(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> getView().onProfileLoaded(account),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void follow(String userId, int direction) {
    Disposable d = userRepository.relationship(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(Throwable::printStackTrace)
        .subscribe();

    getDisposable().add(d);
  }
}
