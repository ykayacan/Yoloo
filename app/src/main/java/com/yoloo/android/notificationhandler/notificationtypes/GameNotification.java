package com.yoloo.android.notificationhandler.notificationtypes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.yoloo.android.R;
import com.yoloo.android.feature.notification.NotificationProvider;
import java.util.Map;

import static com.yoloo.android.notificationhandler.NotificationConstants.GAME_BONUS;
import static com.yoloo.android.notificationhandler.NotificationConstants.GAME_LEVEL_UP;
import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_BOUNTIES;
import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_GAME_ACTION;
import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_LEVEL;
import static com.yoloo.android.notificationhandler.NotificationConstants.KEY_POINTS;

public final class GameNotification implements NotificationProvider {

  private final Map<String, String> data;
  private final Context context;
  private final PendingIntent pendingIntent;

  public GameNotification(Map<String, String> data, Context context, PendingIntent pendingIntent) {
    this.data = data;
    this.context = context;
    this.pendingIntent = pendingIntent;
  }

  @Override
  public Notification getNotification() {
    //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    String notificationContent = handleGameData(data, context.getResources());

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

  @Nullable
  private String handleGameData(Map<String, String> data, Resources res) {
    if (data.get(KEY_GAME_ACTION).equals(GAME_LEVEL_UP)) {
      return res.getString(R.string.label_notification_game_level_up, data.get(KEY_LEVEL));
    } else if (data.get(KEY_GAME_ACTION).equals(GAME_BONUS)) {
      if (data.containsKey(KEY_BOUNTIES) && data.containsKey(KEY_POINTS)) {
        return res.getString(R.string.label_notification_game_together, data.get(KEY_BOUNTIES),
            data.get(KEY_POINTS));
      } else if (data.containsKey(KEY_BOUNTIES) && data.get(KEY_POINTS).isEmpty()) {
        return res.getString(R.string.label_notification_game_with_bounty, data.get(KEY_BOUNTIES));
      } else if (data.containsKey(KEY_POINTS) && data.get(KEY_BOUNTIES).isEmpty()) {
        return res.getString(R.string.label_notification_game_with_points, data.get(KEY_POINTS));
      }
    }
    return null;
  }
}
