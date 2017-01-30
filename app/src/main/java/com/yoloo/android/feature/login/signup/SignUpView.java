package com.yoloo.android.feature.login.signup;

import com.yoloo.android.framework.MvpView;

public interface SignUpView extends MvpView {

  void onUsernameUnAvailable();

  void onSignedUp();

  void onError(Throwable t);

  void onShowLoading();

  void onHideLoading();
}
