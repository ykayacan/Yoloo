package com.yoloo.android.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import com.yoloo.android.YolooApp;

public final class NotificationUtil {

  private NotificationUtil() {
    // empty constructor
  }

  public static void show(Notification notification, int id) {
    NotificationManager notificationManager =
        (NotificationManager) YolooApp.getAppContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(id, notification);
  }

  public static void cancel(int id) {
    NotificationManager notificationManager =
        (NotificationManager) YolooApp.getAppContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(id);
  }
}
