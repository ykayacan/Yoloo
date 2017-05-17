package com.yoloo.android.feature.notification;

import android.app.Notification;

public interface NotificationProvider {

  String TAG = "NotificationProvider";

  Notification getNotification();
}
