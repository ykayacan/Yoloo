package com.yoloo.android.feature.notification;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.feature.base.framework.MvpDataView;
import java.util.List;

public interface NotificationView extends MvpDataView<Response<List<NotificationRealm>>> {
}
