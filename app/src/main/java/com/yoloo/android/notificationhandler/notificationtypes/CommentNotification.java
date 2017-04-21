package com.yoloo.android.notificationhandler.notificationtypes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.yoloo.android.R;
import com.yoloo.android.feature.notification.NotificationProvider;
import java.util.Map;

import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_SENDER_USERNAME;

public final class CommentNotification implements NotificationProvider {

  private final Map<String, String> data;
  private final Context context;
  private final PendingIntent pendingIntent;

  public CommentNotification(Map<String, String> data, Context context,
      PendingIntent pendingIntent) {
    this.data = data;
    this.context = context;
    this.pendingIntent = pendingIntent;
  }

  @Override
  public Notification getNotification() {
    String notificationContent = context
        .getResources()
        .getString(R.string.label_notification_comment, data.get(KEY_SENDER_USERNAME));

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
