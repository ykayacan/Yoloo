package com.yoloo.android.feature.profile;

import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ProfilePresenter extends MvpPresenter<ProfileView> {

  private final UserRepository userRepository;

  public ProfilePresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void loadUserProfile(@Nonnull String userId) {
    Disposable d = userRepository.getUser(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> getView().onProfileLoaded(account),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  public void follow(String userId, int direction) {
    Disposable d = userRepository.follow(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(Throwable::printStackTrace)
        .subscribe();

    getDisposable().add(d);
  }
}
