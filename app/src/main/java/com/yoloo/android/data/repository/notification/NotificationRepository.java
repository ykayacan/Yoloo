package com.yoloo.android.data.repository.notification;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.util.Preconditions;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class NotificationRepository {

  private static NotificationRepository INSTANCE;

  private final NotificationRemoteDataSource remoteDataStore;
  private final NotificationDiskDataSource diskDataStore;

  private NotificationRepository(NotificationRemoteDataSource remoteDataStore,
      NotificationDiskDataSource diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static NotificationRepository getInstance(NotificationRemoteDataSource remoteDataStore,
      NotificationDiskDataSource diskDataStore) {
    if (INSTANCE == null) {
      INSTANCE = new NotificationRepository(remoteDataStore, diskDataStore);
    }
    return INSTANCE;
  }

  public Observable<FcmRealm> registerFcmToken(FcmRealm fcm) {
    Preconditions.checkNotNull(fcm, "Fcm token can not be null.");

    return remoteDataStore.registerFcmToken(fcm)
        .doOnNext(diskDataStore::registerFcmToken)
        .subscribeOn(Schedulers.io());
  }

  public Completable unregisterFcmToken(FcmRealm fcm) {
    Preconditions.checkNotNull(fcm, "Fcm token can not be null.");
    
    return remoteDataStore.unregisterFcmToken(fcm)
        .doOnComplete(diskDataStore::unregisterFcmToken)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<NotificationRealm>>> list(String cursor, String eTag, int limit) {
    return Observable.mergeDelayError(
        diskDataStore.list().subscribeOn(Schedulers.io()),
        remoteDataStore.list(cursor, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io()));
  }
}
