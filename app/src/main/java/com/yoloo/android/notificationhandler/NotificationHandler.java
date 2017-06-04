package com.yoloo.android.notificationhandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.moshi.Moshi;
import com.yoloo.android.notificationhandler.notificationtypes.AcceptNotification;
import com.yoloo.android.notificationhandler.notificationtypes.CommentNotification;
import com.yoloo.android.notificationhandler.notificationtypes.FollowNotification;
import com.yoloo.android.notificationhandler.notificationtypes.GameNotification;
import com.yoloo.android.notificationhandler.notificationtypes.GroupPostNotification;
import com.yoloo.android.notificationhandler.notificationtypes.MentionNotification;
import com.yoloo.android.notificationhandler.notificationtypes.UserPostNotification;
import java.io.IOException;
import java.util.Map;
import timber.log.Timber;

import static com.yoloo.android.notificationhandler.NotificationConstants.ACCEPT;
import static com.yoloo.android.notificationhandler.NotificationConstants.COMMENT;
import static com.yoloo.android.notificationhandler.NotificationConstants.FOLLOW;
import static com.yoloo.android.notificationhandler.NotificationConstants.GAME;
import static com.yoloo.android.notificationhandler.NotificationConstants.MENTION;
import static com.yoloo.android.notificationhandler.NotificationConstants.NEW_FOLLOWER_POST;
import static com.yoloo.android.notificationhandler.NotificationConstants.NEW_GROUP_POST;

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

  public void handle(RemoteMessage message, Context context) throws IOException {
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    Map<String, String> data = message.getData();

    NotificationResponse response = new Moshi.Builder()
        .build()
        .adapter(NotificationResponse.class)
        .fromJson(data.get("values"));

    Timber.d("Action: %s", response);

    notificationManager.notify(0, getNotification(response, context));
  }

  private Notification getNotification(NotificationResponse response, Context context)
      throws IOException {
    switch (response.getAction()) {
      case FOLLOW:
        return new FollowNotification(response, context).getNotification();
      case COMMENT:
        return new CommentNotification(response, context).getNotification();
      case MENTION:
        return new MentionNotification(response, context).getNotification();
      case GAME:
        return new GameNotification(response, context).getNotification();
      case ACCEPT:
        return new AcceptNotification(response, context).getNotification();
      case NEW_FOLLOWER_POST:
        return new UserPostNotification(response, context).getNotification();
      case NEW_GROUP_POST:
        return new GroupPostNotification(response, context).getNotification();
      default:
        return null;
    }
  }
}
