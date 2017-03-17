package com.yoloo.android.feature.notification;

import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.framework.MvpDataView;
import java.util.List;

interface NotificationView extends MvpDataView<List<NotificationRealm>> {

  void showContent();
}
