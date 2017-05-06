package com.yoloo.android.notificationhandler.notificationtypes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.yoloo.android.R;
import com.yoloo.android.feature.base.BaseActivity;
import com.yoloo.android.feature.notification.NotificationProvider;
import com.yoloo.android.notificationhandler.NotificationResponse;

public final class CommentNotification implements NotificationProvider {

  private final NotificationResponse response;
  private final Context context;

  public CommentNotification(NotificationResponse response, Context context) {
    this.response = response;
    this.context = context;
  }

  @Override
  public Notification getNotification() {
    Intent intent = new Intent(context, BaseActivity.class);
    intent.putExtra(NotificationResponse.KEY_ACTION, response.getAction());
    intent.putExtra(NotificationResponse.KEY_POST_ID, response.getPostId());

    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    String content = context
        .getResources()
        .getString(R.string.label_notification_comment, response.getSenderUsername());

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_yoloo_notification)
        .setContentTitle("Yoloo")
        .setContentText(content)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .setContentIntent(pendingIntent);

    return builder.build();
  }
}
