package com.yoloo.android.data.repository.notification.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.model.NotificationRealmFields;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;

public class NotificationDiskDataSource {

  private static NotificationDiskDataSource instance;

  private NotificationDiskDataSource() {
  }

  public static NotificationDiskDataSource getInstance() {
    if (instance == null) {
      instance = new NotificationDiskDataSource();
    }
    return instance;
  }

  public void registerFcmToken(FcmRealm fcm) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(fcm));
    realm.close();
  }

  public void unregisterFcmToken() {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.delete(FcmRealm.class));
    realm.close();
  }

  public void addAll(List<NotificationRealm> notifications) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(notifications));
    realm.close();
  }

  public Observable<Response<List<NotificationRealm>>> list() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<NotificationRealm> results = realm.where(NotificationRealm.class)
          .findAllSorted(NotificationRealmFields.CREATED, Sort.DESCENDING);

      if (results.isEmpty()) {
        realm.close();
        return Response.create(Collections.emptyList(), null, null);
      } else {
        List<NotificationRealm> notifications = realm.copyFromRealm(results);
        realm.close();
        return Response.create(notifications, null, null);
      }
    });
  }
}
