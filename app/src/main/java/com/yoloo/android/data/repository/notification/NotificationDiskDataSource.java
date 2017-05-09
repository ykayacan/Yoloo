package com.yoloo.android.data.repository.notification;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.FcmRealm;
import com.yoloo.android.data.db.NotificationRealm;
import com.yoloo.android.data.db.NotificationRealmFields;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;

class NotificationDiskDataSource {

  private static NotificationDiskDataSource instance;

  private NotificationDiskDataSource() {
  }

  static NotificationDiskDataSource getInstance() {
    if (instance == null) {
      instance = new NotificationDiskDataSource();
    }
    return instance;
  }

  void registerFcmToken(FcmRealm fcm) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(fcm));
    realm.close();
  }

  void unregisterFcmToken() {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.delete(FcmRealm.class));
    realm.close();
  }

  void addAll(List<NotificationRealm> notifications) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(notifications));
    realm.close();
  }

  Observable<Response<List<NotificationRealm>>> list(int limit) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<NotificationRealm> results = realm.where(NotificationRealm.class)
          .findAllSorted(NotificationRealmFields.CREATED, Sort.DESCENDING);

      List<NotificationRealm> notifications =
          results.isEmpty() ? Collections.emptyList() : realm.copyFromRealm(results);

      realm.close();

      return Response.create(notifications, null);
    });
  }
}
