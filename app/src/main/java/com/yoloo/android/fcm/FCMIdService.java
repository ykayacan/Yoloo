package com.yoloo.android.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.yoloo.android.data.db.FcmRealm;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * This service do the unique task ; save FCM Id token locally & remotely
 */
public class FCMIdService extends FirebaseInstanceIdService {

  private Disposable disposable;

  /**
   * Called if InstanceID token is updated. This may occur if the security from the previous token
   * had
   * been compromised. Note that this is called when the InstanceID token is initially generated so
   * this is where you would retrieve the token.
   */
  @Override public void onTokenRefresh() {
    String refreshedToken = FirebaseInstanceId.getInstance().getToken();

    sendTokenToServer(refreshedToken);
  }

  private void sendTokenToServer(String refreshedToken) {
    disposable = NotificationRepositoryProvider
        .getRepository()
        .registerFcmToken(new FcmRealm(refreshedToken))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
