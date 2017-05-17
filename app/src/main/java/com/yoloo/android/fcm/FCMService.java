package com.yoloo.android.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yoloo.android.notificationhandler.NotificationHandler;
import java.io.IOException;
import timber.log.Timber;

public class FCMService extends FirebaseMessagingService {

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  @Override public void onMessageReceived(RemoteMessage remoteMessage) {
    Timber.d("onMessage(): %s", remoteMessage.getData().toString());

    try {
      NotificationHandler.getInstance().handle(remoteMessage, this);
    } catch (IOException e) {
      Timber.e(e);
    }
  }
}
