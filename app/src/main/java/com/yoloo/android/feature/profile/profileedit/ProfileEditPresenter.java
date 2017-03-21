package com.yoloo.android.feature.profile.profileedit;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class ProfileEditPresenter extends MvpPresenter<ProfileEditView> {

  private final UserRepository userRepository;

  ProfileEditPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(ProfileEditView view) {
    super.onAttachView(view);
    loadMe();
  }

  private void loadMe() {
    Disposable d = userRepository.getMe()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> getView().onLoaded(account),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void updateMe(@Nonnull AccountRealm account) {
    Disposable d = userRepository.updateMe(account)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(updated -> getView().onAccountUpdated(updated),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void checkUsername(@Nonnull String username) {
    Timber.d("checkUsername(): %s", username);
    Disposable d = userRepository.checkUsername(username)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(available -> {
          if (!available) {
            getView().onUsernameUnavailable();
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
