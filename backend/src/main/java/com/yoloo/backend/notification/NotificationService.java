package com.yoloo.backend.notification;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.notification.type.Notifiable;
import com.yoloo.backend.util.NetworkHelper;
import com.yoloo.backend.util.ServerConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class NotificationService {

  private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

  private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";

  private URLFetchService service;

  /**
   * Send.
   *
   * @param notifiable the bundle
   */
  public void send(@Nonnull Notifiable notifiable) {
    if (!ServerConfig.isDev()) {
      try {
        service.fetchAsync(buildRequest(notifiable.getPushMessage().getJsonAsBytes()));
      } catch (IOException e) {
        LOG.info(e.getMessage());
      }
    }
  }

  private HTTPRequest buildRequest(byte[] bytes) throws MalformedURLException {
    final URL url = new URL(FCM_ENDPOINT);
    final HTTPRequest request = NetworkHelper.INSTANCE.getRequest(url, HTTPMethod.POST);

    request.addHeader(new HTTPHeader(OAuth2.HeaderType.AUTHORIZATION,
        "key=" + System.getProperty("fcm.api.key")));
    request.addHeader(new HTTPHeader(OAuth2.HeaderType.CONTENT_TYPE, OAuth2.ContentType.JSON));
    request.setPayload(bytes);

    return request;
  }
}