package com.yoloo.android.feature.group.groupuserslist;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface GroupUsersListView extends MvpDataView<List<AccountRealm>> {
  void onFollowedSuccessfully();

  void onUnfollowedSuccessfully();
}
