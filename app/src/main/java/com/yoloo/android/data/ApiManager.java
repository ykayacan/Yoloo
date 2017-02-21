package com.yoloo.android.data;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.yoloo.backend.yolooApi.YolooApi;
import io.reactivex.Single;

public enum ApiManager {
  INSTANCE;

  public YolooApi getApi() {
    return LazyApiHolder.YOLOO_API;
  }

  static class LazyApiHolder {
    // TODO: 18.02.2017 Convert it to OkHttp
    // see https://github.com/dereulenspiegel/okhttp-transport/blob/master/
    // okhttp-transport/src/main/java/de/akuz/google/api/OkHttpTransport.java
    private static final YolooApi YOLOO_API = new YolooApi.Builder(
        new NetHttpTransport(), JacksonFactory.getDefaultInstance(), null)
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

      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      if (user != null) {
        user.getToken(true)
            .addOnCompleteListener(onCompleteListener);
      }
    });
  }
}
