package com.yoloo.android.util;

import android.content.Context;
import android.content.res.Resources;
import com.yoloo.android.R;
import java.util.Map;
import timber.log.Timber;

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
    final String action = data.get("act");

    Timber.d("Action: %s", action);

    switch (action) {
      case FOLLOW:
        return res.getString(R.string.label_notification_follow, data.get("sU"));
      case COMMENT:
        return res.getString(R.string.label_notification_comment, data.get("sU"));
      case MENTION:
        return res.getString(R.string.label_notification_mention, data.get("sU"), data.get("c"));
      case GAME:
        return "";
      case ACCEPT:
        return res.getString(R.string.label_notification_accept);
      default:
        return "";
    }
  }
}
