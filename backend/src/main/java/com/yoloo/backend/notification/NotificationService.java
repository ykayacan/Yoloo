package com.yoloo.backend.notification;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.yoloo.backend.authentication.oauth2.OAuth2;
import com.yoloo.backend.notification.type.NotificationBundle;
import com.yoloo.backend.util.ServerConfig;
import com.yoloo.backend.util.NetworkHelper;
import io.reactivex.Single;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class NotificationService {

  private static final Logger logger =
      Logger.getLogger(NotificationService.class.getName());

  private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";

  @NonNull
  private URLFetchService service;

  /**
   * Send.
   *
   * @param bundle the bundle
   */
  public void send(NotificationBundle bundle) {
    if (!ServerConfig.isDev()) {
      Single
          .create(e -> {
            try {
              e.onSuccess(bundle.getPushMessage().getJsonAsBytes());
            } catch (IOException t) {
              e.onError(t);
            }
          })
          .cast(byte[].class)
          .map(this::buildRequest)
          .doOnSuccess(httpRequest -> service.fetchAsync(httpRequest));
    }
  }

  private HTTPRequest buildRequest(byte[] bytes) throws MalformedURLException {
    final URL url = new URL(FCM_ENDPOINT);
    final HTTPRequest request = NetworkHelper.INSTANCE.getRequest(url, HTTPMethod.POST);

    request.addHeader(new HTTPHeader(OAuth2.HeaderType.AUTHORIZATION,
        "key=" + System.getProperty("gcm.api.key")));
    request.addHeader(new HTTPHeader(OAuth2.HeaderType.CONTENT_TYPE, OAuth2.ContentType.JSON));
    request.setPayload(bytes);

    return request;
  }
}