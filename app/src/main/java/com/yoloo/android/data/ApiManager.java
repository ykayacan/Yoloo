package com.yoloo.android.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.yoloo.android.Constants;
import com.yoloo.android.backend.modal.yolooApi.YolooApi;
import io.reactivex.Single;

public enum ApiManager {
  INSTANCE;

  public YolooApi getApi() {
    return LazyApiHolder.YOLOO_API;
  }

  static class LazyApiHolder {
    private static final YolooApi YOLOO_API = new YolooApi.Builder(
        AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), null)
        .setRootUrl(Constants.API_BASEURL)
        .setGoogleClientRequestInitializer(ApiManager::checkGZip)
        .setApplicationName("YolooApp")
        .build();
  }

  private static void checkGZip(AbstractGoogleClientRequest<?> request) {
    // only enable GZip when connecting to remove server
    final boolean enableGZip = Constants.API_BASEURL.startsWith("https:");
    if (!enableGZip) {
      request.setDisableGZipContent(true);
    }
  }

  public static Single<String> getIdToken() {
    return Single.create(e -> {
      final OnCompleteListener<GetTokenResult> onCompleteListener = task -> {
        if (task.isSuccessful()) {
          e.onSuccess(task.getResult().getToken());
        } else {
          e.onError(task.getException());
        }
      };

      FirebaseAuth.getInstance().getCurrentUser().getToken(true)
          .addOnCompleteListener(onCompleteListener);
    });
  }
}