package com.yoloo.android.notificationhandler.notificationtypes;

/*public final class MentionNotification implements NotificationProvider {

  private final NotificationResponse response;
  private final Context context;
  private final PendingIntent pendingIntent;

  public MentionNotification(NotificationResponse response, Context context,
      PendingIntent pendingIntent) {
    this.response = response;
    this.context = context;
    this.pendingIntent = pendingIntent;
  }

  @Override
  public Notification getNotification() {
    String notificationContent = context
        .getResources()
        .getString(R.string.label_notification_mention, response.getSenderUsername(),
            data.get(KEY_COMMENT));

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
}*/
