package com.yoloo.android.feature.auth.signupdiscover;

import com.yoloo.android.framework.MvpView;

interface SignUpDiscoverView extends MvpView {

  void onError(Throwable t);

  void onShowLoading();

  void onHideLoading();

  void onSignedUp();
}
