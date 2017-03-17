package com.yoloo.android.fcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class FCMManager {

  private static FCMManager instance;

  private FCMListener fcmListener;

  /**
   * Message receiver onReceive method called when registration ID(Token) is available
   */
  private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (FCMPrefsHelper.hasFCMToken(context) && fcmListener != null) {
        fcmListener.onDeviceRegistered(FCMPrefsHelper.getFCMToken(context));
      }
    }
  };

  /**
   * Private constructor
   */
  private FCMManager(Context context) {
    init(context);
  }

  /**
   * Singleton instance method
   */
  public static FCMManager getInstance(Context context) {
    if (instance == null) {
      instance = new FCMManager(context);
    }
    return instance;
  }

  /**
   * Register listener
   */
  public void register(FCMListener fcmListener) {
    this.fcmListener = fcmListener;
  }

  /**
   * Unregister listener. No longer need to notify.
   */
  public void unRegister() {
    fcmListener = null;
  }

  /**
   * Initializes shared preferences, checks google play services if available, and register device
   * to FCM server.
   */
  public void init(Context context) {
    LocalBroadcastManager.getInstance(context)
        .registerReceiver(messageReceiver, new IntentFilter(FCMPrefsHelper.REGISTRATION_COMPLETE));

    /*
      check if device has Google play service on it
     */
    GoogleApiAvailability api = GoogleApiAvailability.getInstance();
    final int status = api.isGooglePlayServicesAvailable(context);

    /*
      If true, process the {@link SaveFCMIdService}
     */
    if (status == ConnectionResult.SUCCESS) {
      context.startService(new Intent(context, SaveFCMIdService.class));
    } else {
      if (fcmListener != null) {
        fcmListener.onPlayServiceError();
      }
    }
  }

  /**
   * Called by service when message received. Notify Listener if it s not null.
   */
  public void onMessage(RemoteMessage remoteMessage) {
    if (fcmListener != null) {
      fcmListener.onMessage(remoteMessage);
    }
  }

  /**
   * Subscribe to topic
   */
  public void subscribeTopic(Context context, String topicName) {
    if (FCMPrefsHelper.hasFCMToken(context)) {
      FirebaseMessaging.getInstance().subscribeToTopic(topicName);
    }
  }

  /**
   * Unsucbsribe from topic
   */
  public void unSubscribeTopic(Context context, String topicName) {
    if (FCMPrefsHelper.hasFCMToken(context)) {
      FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName);
    }
  }
}
