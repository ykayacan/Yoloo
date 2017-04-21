package com.yoloo.android.feature.category;

import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.framework.MvpView;
import java.util.List;

interface GroupOverviewView extends MvpView {

  void onGroupsLoaded(List<GroupRealm> categories);

  void onError(Throwable throwable);
}
