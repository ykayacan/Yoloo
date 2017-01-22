package com.yoloo.backend.notification.type;

import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import java.util.List;

public interface NotificationBundle {

  List<Notification> getNotifications();

  PushMessage getPushMessage();
}
