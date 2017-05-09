package com.yoloo.android.feature.group;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.framework.MvpDataView;

interface GroupView extends MvpDataView<GroupRealm> {

  void onAccountLoaded(AccountRealm account);
}
