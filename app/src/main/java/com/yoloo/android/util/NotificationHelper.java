package com.yoloo.android.util;

import android.content.Context;
import android.content.res.Resources;
import com.yoloo.android.R;
import java.util.Map;

public final class NotificationHelper {

  public static final String FOLLOW = "1";
  public static final String COMMENT = "2";
  public static final String MENTION = "3";
  public static final String GAME = "4";
  public static final String ACCEPT = "5";

  private NotificationHelper() {
  }

  public static String getRelatedNotificationString(Context context, Map<String, String> data) {
    final Resources res = context.getResources();
    final String action = data.get("action");

    switch (action) {
      case FOLLOW:
        return res.getString(R.string.label_notification_follow, data.get("senderUsername"));
      case COMMENT:
        return res.getString(R.string.label_notification_comment, data.get("senderUsername"));
      case MENTION:
        return res.getString(R.string.label_notification_mention, data.get("senderUsername"),
            data.get("message"));
      case GAME:
        return "";
      case ACCEPT:
        return res.getString(R.string.label_notification_accept);
      default:
        return "";
    }
  }
}
