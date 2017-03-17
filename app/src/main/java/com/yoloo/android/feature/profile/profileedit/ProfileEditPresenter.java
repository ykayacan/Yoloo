package com.yoloo.android.feature.profile.profileedit;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ProfileEditPresenter extends MvpPresenter<ProfileEditView> {

  private final UserRepository userRepository;

  public ProfileEditPresenter(UserRepository userRepository) {
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

  public void updateMe(@Nonnull AccountRealm account) {
    Disposable d = userRepository.updateUser(account)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(updated -> getView().onAccountUpdated(updated),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
