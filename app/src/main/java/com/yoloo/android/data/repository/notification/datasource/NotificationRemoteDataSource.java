package com.yoloo.android.data.repository.notification.datasource;

import com.yoloo.android.data.ApiManager;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.model.NotificationRealm;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;

public class NotificationRemoteDataSource {

  private static NotificationRemoteDataSource INSTANCE;

  private NotificationRemoteDataSource() {
  }

  public static NotificationRemoteDataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new NotificationRemoteDataSource();
    }
    return INSTANCE;
  }

  public Observable<FcmRealm> registerFcmToken(FcmRealm fcm) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.just(fcm));
  }

  public Completable unregisterFcmToken(FcmRealm fcm) {
    return ApiManager.getIdToken()
        .flatMap(s -> Single.just(fcm.getToken()))
        .toCompletable();
  }

  public Observable<Response<List<NotificationRealm>>> list(String cursor, int limit) {
    return ApiManager.getIdToken()
        .toObservable()
        .flatMap(s -> Observable.empty());
  }
}
