package com.yoloo.android.data.repository.user.datasource;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import java.util.List;

public class UserDiskDataStore {

  private static UserDiskDataStore INSTANCE;

  private UserDiskDataStore() {
  }

  public static UserDiskDataStore getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new UserDiskDataStore();
    }
    return INSTANCE;
  }

  public Single<AccountRealm> get(String userId) {
    Realm realm = Realm.getDefaultInstance();
    AccountRealm account = realm.copyFromRealm(
        realm.where(AccountRealm.class).equalTo(AccountRealmFields.ID, userId).findFirst());
    realm.close();

    return Single.just(account);
  }

  public Single<AccountRealm> getMe() {
    Realm realm = Realm.getDefaultInstance();
    AccountRealm account = realm.copyFromRealm(
        realm.where(AccountRealm.class).equalTo(AccountRealmFields.ME, true).findFirst());
    realm.close();

    return Single.just(account);
  }

  public void add(AccountRealm account) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> tx.insertOrUpdate(account));
    realm.close();
  }

  public void delete(String userId) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> {
      AccountRealm account =
          tx.where(AccountRealm.class).equalTo(AccountRealmFields.ID, userId).findFirstAsync();

      if (account.isValid() && account.isLoaded()) {
        account.deleteFromRealm();
      }
    });
    realm.close();
  }

  public Observable<List<AccountRealm>> listRecent() {
    return Observable.create(e -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<AccountRealm> results = realm.where(AccountRealm.class)
          .equalTo(AccountRealmFields.RECENT, true)
          .findAllAsync();

      final RealmChangeListener<RealmResults<AccountRealm>> listener = element -> {
        e.onNext(realm.copyFromRealm(results));
        e.onComplete();

        realm.close();
      };

      results.addChangeListener(listener);

      e.setCancellable(() -> results.removeChangeListener(listener));
    });
  }
}