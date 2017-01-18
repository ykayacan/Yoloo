package com.yoloo.backend.notification.type;

import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;

public interface NotificationBundle {

  Notification getNotification();

  PushMessage getPushMessage();
}
