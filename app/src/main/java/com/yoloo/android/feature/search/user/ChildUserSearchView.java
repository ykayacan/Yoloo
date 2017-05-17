package com.yoloo.android.feature.search.user;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface ChildUserSearchView extends MvpView {

  void onRecentUsersLoaded(List<AccountRealm> accounts);

  void onUsersLoaded(List<AccountRealm> accounts);
}
