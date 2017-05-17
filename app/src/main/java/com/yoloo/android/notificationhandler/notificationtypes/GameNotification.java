package com.yoloo.android.notificationhandler.notificationtypes;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.yoloo.android.R;
import com.yoloo.android.feature.notification.NotificationProvider;
import com.yoloo.android.notificationhandler.NotificationResponse;

import static com.yoloo.android.notificationhandler.NotificationConstants.GAME_BONUS;
import static com.yoloo.android.notificationhandler.NotificationConstants.GAME_LEVEL_UP;

public final class GameNotification implements NotificationProvider {

  private final NotificationResponse response;
  private final Context context;

  public GameNotification(NotificationResponse data, Context context) {
    this.response = data;
    this.context = context;
  }

  @Override
  public Notification getNotification() {
    broadcastNewPostEvent();

    //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String content = handleGameData(response, context.getResources());

    int primaryColor = ContextCompat.getColor(context, R.color.primary);

    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
        R.drawable.ic_trophy_cup_silhouette_32dp);

    return new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_yoloo_notification)
        .setContentTitle("Yoloo")
        .setContentText(content)
        .setAutoCancel(true)
        .setLargeIcon(bitmap)
        .setDefaults(Notification.DEFAULT_ALL)
        .setColor(primaryColor)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
        .build();
  }

  @Nullable
  private String handleGameData(NotificationResponse response, Resources res) {
    if (response.getGameAction().equals(GAME_LEVEL_UP)) {
      return res.getString(R.string.label_notification_game_level_up, response.getLevel());
    } else if (response.getGameAction().equals(GAME_BONUS)) {
      if (response.getBounties() != null && response.getPoints() != null) {
        return res.getString(R.string.label_notification_game_together, response.getBounties(),
            response.getPoints());
      } else if (response.getBounties() != null
          && response.getPoints() == null
          && !response.getPoints().equals("0")) {
        return res.getString(R.string.label_notification_game_with_bounty, response.getBounties());
      } else if (response.getPoints() != null
          && response.getBounties() == null
          && !response.getBounties().equals("0")) {
        return res.getString(R.string.label_notification_game_with_points, response.getPoints());
      }
    }
    return null;
  }

  private void broadcastNewPostEvent() {
    Intent intent = new Intent(NotificationProvider.TAG);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
  }
}
