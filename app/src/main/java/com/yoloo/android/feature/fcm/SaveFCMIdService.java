package com.yoloo.android.feature.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Intent Service which manage the Id Registration to local & remote
 */
public class SaveFCMIdService extends IntentService {

  /**
   * Class tag
   */
  private static final String TAG = "SaveFCMIdService.class.getSimpleName()";

  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   */
  public SaveFCMIdService() {
    super(TAG);
  }

  /**
   * This Intent is responsible for getPost token from FCM server and send broadcast. After we getPost
   * token, we save this token to shared preferences.
   */
  @Override protected void onHandleIntent(Intent intent) {
    try {
      String refreshedToken = FirebaseInstanceId.getInstance().getToken();
      FCMPrefsHelper.saveFCMToken(this, refreshedToken);

      /**
       * You should store a boolean that indicates whether the generated token has been
       * sent to your server. If the boolean is false, send the token to your server,
       * otherwise your server should have already received the token.
       */
      FCMPrefsHelper.sendFCMTokenToServer(this, true);
    } catch (Exception e) {
      FCMPrefsHelper.sendFCMTokenToServer(this, false);
    }

    LocalBroadcastManager.getInstance(this)
        .sendBroadcast(new Intent(FCMPrefsHelper.REGISTRATION_COMPLETE));
  }
}
