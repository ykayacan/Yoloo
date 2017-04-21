package com.yoloo.android.data;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.BuildConfig;
import com.yoloo.backend.yolooApi.YolooApi;
import io.reactivex.Single;
import okhttp3.OkHttpClient;

public enum ApiManager {
  INSTANCE;

  private static void checkGZip(AbstractGoogleClientRequest<?> request) {
    // only enable GZip when connecting to remove server
    final boolean enableGZip = BuildConfig.SERVER_URL.startsWith("https:");
    request.setDisableGZipContent(!enableGZip);
  }

  public static Single<String> getIdToken() {
    return Single.create(e -> {
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      if (user != null) {
        try {
          user.getToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              e.onSuccess(task.getResult().getToken());
            } else {
              e.onError(task.getException());
            }
          });
        } catch (Exception ex) {
          e.onError(ex);
        }
      }
    });
  }

  public YolooApi getApi() {
    return LazyApiHolder.YOLOO_API;
  }

  public OkHttpClient getOkHttpClient() {
    return LazyOkHttpClient.CLIENT;
  }

  static class LazyApiHolder {
    static final YolooApi YOLOO_API =
        new YolooApi.Builder(LazyHttpTransport.HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(),
            null)
            .setRootUrl(BuildConfig.SERVER_URL)
            .setGoogleClientRequestInitializer(ApiManager::checkGZip)
            .setApplicationName("YolooApp")
            .build();
  }

  static class LazyOkHttpClient {
    static final OkHttpClient CLIENT = new OkHttpClient();
  }

  private static class LazyHttpTransport {
    static final HttpTransport HTTP_TRANSPORT = new OkHttpTransport();
  }
}
