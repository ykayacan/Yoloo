package com.yoloo.android.notificationhandler.notificationtypes;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
    //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String content = handleGameData(response, context.getResources());

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_yoloo_notification)
        .setContentTitle("Yoloo")
        .setContentText(content)
        .setAutoCancel(true)
        .setDefaults(Notification.DEFAULT_ALL)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

    return builder.build();
  }

  @Nullable
  private String handleGameData(NotificationResponse response, Resources res) {
    if (response.getGameAction().equals(GAME_LEVEL_UP)) {
      return res.getString(R.string.label_notification_game_level_up, response.getLevel());
    } else if (response.getGameAction().equals(GAME_BONUS)) {
      if (response.getBounties() != null && response.getPoints() != null) {
        return res.getString(R.string.label_notification_game_together, response.getBounties(),
            response.getPoints());
      } else if (response.getBounties() != null && response.getPoints() == null) {
        return res.getString(R.string.label_notification_game_with_bounty, response.getBounties());
      } else if (response.getPoints() != null && response.getBounties() == null) {
        return res.getString(R.string.label_notification_game_with_points, response.getPoints());
      }
    }
    return null;
  }
}
