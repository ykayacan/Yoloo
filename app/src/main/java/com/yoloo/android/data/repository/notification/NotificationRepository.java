package com.yoloo.android.data.repository.notification;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.model.NotificationRealm;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NotificationRepository {

  private static NotificationRepository instance;

  private final NotificationRemoteDataSource remoteDataStore;
  private final NotificationDiskDataSource diskDataStore;

  private NotificationRepository(NotificationRemoteDataSource remoteDataStore,
      NotificationDiskDataSource diskDataStore) {
    this.remoteDataStore = remoteDataStore;
    this.diskDataStore = diskDataStore;
  }

  public static NotificationRepository getInstance(NotificationRemoteDataSource remoteDataStore,
      NotificationDiskDataSource diskDataStore) {
    if (instance == null) {
      instance = new NotificationRepository(remoteDataStore, diskDataStore);
    }
    return instance;
  }

  public Completable registerFcmToken(@Nonnull FcmRealm fcm) {
    return remoteDataStore
        .registerFcmToken(fcm)
        .doOnComplete(() -> diskDataStore.registerFcmToken(fcm))
        .subscribeOn(Schedulers.io());
  }

  public Completable unregisterFcmToken(@Nonnull FcmRealm fcm) {
    return remoteDataStore
        .unregisterFcmToken(fcm)
        .doOnComplete(diskDataStore::unregisterFcmToken)
        .subscribeOn(Schedulers.io());
  }

  public Observable<Response<List<NotificationRealm>>> listNotifications(@Nullable String cursor,
      int limit) {
    return Observable.mergeDelayError(diskDataStore.list(limit).subscribeOn(Schedulers.io()),
        remoteDataStore
            .list(cursor, limit)
            .doOnNext(response -> diskDataStore.addAll(response.getData()))
            .subscribeOn(Schedulers.io()));
  }
}
