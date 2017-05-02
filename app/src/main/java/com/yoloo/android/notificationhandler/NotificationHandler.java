package com.yoloo.android.notificationhandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.moshi.Moshi;
import com.yoloo.android.notificationhandler.notificationtypes.AcceptNotification;
import com.yoloo.android.notificationhandler.notificationtypes.CommentNotification;
import com.yoloo.android.notificationhandler.notificationtypes.FollowNotification;
import com.yoloo.android.notificationhandler.notificationtypes.GameNotification;
import java.io.IOException;
import java.util.Map;
import timber.log.Timber;

import static com.yoloo.android.notificationhandler.NotificationConstants.ACCEPT;
import static com.yoloo.android.notificationhandler.NotificationConstants.COMMENT;
import static com.yoloo.android.notificationhandler.NotificationConstants.FOLLOW;
import static com.yoloo.android.notificationhandler.NotificationConstants.GAME;
import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_VALUES;
import static com.yoloo.android.notificationhandler.NotificationConstants.MENTION;

public final class NotificationHandler {

  private static NotificationHandler instance;

  private NotificationHandler() {
  }

  public static NotificationHandler getInstance() {
    if (instance == null) {
      instance = new NotificationHandler();
    }
    return instance;
  }

  public void handle(RemoteMessage message, Context context, Intent intentToPending)
      throws IOException {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, intentToPending, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0, getNotification(message, context, pendingIntent));
  }

  private Notification getNotification(RemoteMessage message, Context context, PendingIntent intent)
      throws IOException {
    Map<String, String> data = message.getData();

    Moshi moshi = new Moshi.Builder().build();
    NotificationResponse response =
        moshi.adapter(NotificationResponse.class).fromJson(data.get(KEY_VALUES));

    Timber.d("Action: %s", response);

    switch (response.getAction()) {
      case FOLLOW:
        return new FollowNotification(response, context, intent).getNotification();
      case COMMENT:
        return new CommentNotification(response, context, intent).getNotification();
      case MENTION:
        //return new MentionNotification(response, context, intent).getNotification();
        return null;
      case GAME:
        return new GameNotification(response, context, intent).getNotification();
      case ACCEPT:
        return new AcceptNotification(data, context, intent).getNotification();
      default:
        return null;
    }
  }
}
