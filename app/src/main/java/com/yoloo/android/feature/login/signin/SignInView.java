package com.yoloo.android.feature.login.signin;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.feature.base.framework.MvpView;

public interface SignInView extends MvpView {

  void onSignedIn(AccountRealm account);

  void onError(Throwable t);

  void onShowLoading();

  void onHideLoading();
}
