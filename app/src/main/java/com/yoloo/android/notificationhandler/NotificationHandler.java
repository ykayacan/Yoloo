package com.yoloo.android.notificationhandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.messaging.RemoteMessage;
import com.yoloo.android.notificationhandler.notificationtypes.AcceptNotification;
import com.yoloo.android.notificationhandler.notificationtypes.CommentNotification;
import com.yoloo.android.notificationhandler.notificationtypes.FollowNotification;
import com.yoloo.android.notificationhandler.notificationtypes.GameNotification;
import com.yoloo.android.notificationhandler.notificationtypes.MentionNotification;
import java.util.Map;
import timber.log.Timber;

import static com.yoloo.android.notificationhandler.NotificationConstants.ACCEPT;
import static com.yoloo.android.notificationhandler.NotificationConstants.COMMENT;
import static com.yoloo.android.notificationhandler.NotificationConstants.FOLLOW;
import static com.yoloo.android.notificationhandler.NotificationConstants.GAME;
import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_ACTION;
import static com.yoloo.android.notificationhandler.NotificationConstants.MENTION;

public final class NotificationHandler {

  private static NotificationHandler instance;

  private Map<String, String> data;

  private NotificationHandler(RemoteMessage remoteMessage) {
    data = remoteMessage.getData();
  }

  public static NotificationHandler getInstance(RemoteMessage remoteMessage) {
    if (instance == null) {
      instance = new NotificationHandler(remoteMessage);
    }
    return instance;
  }

  public void handle(Context context, Intent intentToPending) {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, intentToPending, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0, getNotification(context, pendingIntent));
  }

  private Notification getNotification(Context context, PendingIntent intent) {
    final String action = data.get(KEY_ACTION);
    Timber.d("Action: %s", action);

    switch (action) {
      case FOLLOW:
        return new FollowNotification(data, context, intent).getNotification();
      case COMMENT:
        return new CommentNotification(data, context, intent).getNotification();
      case MENTION:
        return new MentionNotification(data, context, intent).getNotification();
      case GAME:
        return new GameNotification(data, context, intent).getNotification();
      case ACCEPT:
        return new AcceptNotification(data, context, intent).getNotification();
      default:
        return null;
    }
  }
}
