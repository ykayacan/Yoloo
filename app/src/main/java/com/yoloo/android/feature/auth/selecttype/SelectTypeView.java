package com.yoloo.android.feature.auth.selecttype;

import com.yoloo.android.framework.MvpView;

interface SelectTypeView extends MvpView {

  void onError(Throwable t);

  void onShowLoading();

  void onHideLoading();

  void onSignedUp();
}
