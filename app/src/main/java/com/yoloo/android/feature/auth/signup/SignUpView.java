package com.yoloo.android.feature.auth.signup;

import com.yoloo.android.framework.MvpView;

public interface SignUpView extends MvpView {

  void onCheckUsername(boolean available);

  void onSignedUp();

  void onError(Throwable t);

  void onShowLoading();

  void onHideLoading();
}
