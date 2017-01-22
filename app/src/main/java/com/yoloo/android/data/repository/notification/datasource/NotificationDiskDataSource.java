package com.yoloo.android.data.repository.notification.datasource;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.model.NotificationRealm;
import com.yoloo.android.data.model.NotificationRealmFields;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.List;

public class NotificationDiskDataSource {

  private static NotificationDiskDataSource INSTANCE;

  private NotificationDiskDataSource() {
  }

  public static NotificationDiskDataSource getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new NotificationDiskDataSource();
    }
    return INSTANCE;
  }

  public void registerFcmToken(FcmRealm fcm) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> tx.insertOrUpdate(fcm));
    realm.close();
  }

  public void unregisterFcmToken() {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> tx.delete(FcmRealm.class));
    realm.close();
  }

  public void addAll(List<NotificationRealm> notifications) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> tx.insertOrUpdate(notifications));
    realm.close();
  }

  public Observable<Response<List<NotificationRealm>>> list() {
    return Observable.create(e -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<NotificationRealm> results = realm.where(NotificationRealm.class)
          .findAllSortedAsync(NotificationRealmFields.CREATED, Sort.DESCENDING);

      final RealmChangeListener<RealmResults<NotificationRealm>> listener = element -> {
        e.onNext(Response.create(realm.copyFromRealm(element), null, null));
        e.onComplete();

        realm.close();
      };

      results.addChangeListener(listener);

      e.setCancellable(() -> results.removeChangeListener(listener));
    });
  }
}
