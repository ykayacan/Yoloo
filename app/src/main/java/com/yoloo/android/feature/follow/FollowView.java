package com.yoloo.android.feature.follow;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface FollowView extends MvpDataView<List<AccountRealm>> {

  void onLoadedMore(List<AccountRealm> accounts);
}
