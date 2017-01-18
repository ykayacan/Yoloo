package com.yoloo.backend.notification;

import com.google.appengine.api.urlfetch.URLFetchService;

public class NotificationServiceFactory {

  public static NotificationService getNotificationService(URLFetchService service) {
    return NotificationService.create(service);
  }
}
