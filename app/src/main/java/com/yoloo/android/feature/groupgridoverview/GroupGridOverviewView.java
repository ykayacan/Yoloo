package com.yoloo.android.feature.groupgridoverview;

import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface GroupGridOverviewView extends MvpView {

  void onGroupsLoaded(List<GroupRealm> categories);

  void onError(Throwable throwable);
}
