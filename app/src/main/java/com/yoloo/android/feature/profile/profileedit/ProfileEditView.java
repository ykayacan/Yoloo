package com.yoloo.android.feature.profile.profileedit;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.framework.MvpDataView;

public interface ProfileEditView extends MvpDataView<AccountRealm> {

  void onAccountUpdated(AccountRealm account);

  void onUsernameUnavailable();

  void onShowLoading();

  void onHideLoading();
}
