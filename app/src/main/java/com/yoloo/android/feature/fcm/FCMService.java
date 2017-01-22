package com.yoloo.android.feature.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Receiver which trigger action on {@link FCMListener}
 */
public class FCMService extends FirebaseMessagingService {

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  @Override public void onMessageReceived(RemoteMessage remoteMessage) {
    FCMManager.getInstance(getApplicationContext()).onMessage(remoteMessage);
  }
}
