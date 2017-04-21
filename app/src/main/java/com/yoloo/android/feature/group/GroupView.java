package com.yoloo.android.feature.group;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.framework.MvpDataView;

interface GroupView extends MvpDataView<GroupRealm> {

  void onAccountLoaded(AccountRealm account);
}
