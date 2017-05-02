package com.yoloo.android.notificationhandler.notificationtypes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.yoloo.android.R;
import com.yoloo.android.feature.notification.NotificationProvider;
import com.yoloo.android.notificationhandler.NotificationResponse;

public final class FollowNotification implements NotificationProvider {

  private final NotificationResponse response;
  private final Context context;
  private final PendingIntent pendingIntent;

  public FollowNotification(NotificationResponse response, Context context,
      PendingIntent pendingIntent) {
    this.response = response;
    this.context = context;
    this.pendingIntent = pendingIntent;
  }

  @Override
  public Notification getNotification() {
    String notificationContent = context
        .getResources()
        .getString(R.string.label_notification_follow, response.getSenderUsername());

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Yoloo")
        .setContentText(notificationContent)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
        .setContentIntent(pendingIntent);

    return notificationBuilder.build();
  }
}
