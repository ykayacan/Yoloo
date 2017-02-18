package com.yoloo.android.data.repository.user.datasource;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import java.util.Collections;
import java.util.List;

public class UserDiskDataStore {

  private static UserDiskDataStore instance;

  private UserDiskDataStore() {
  }

  public static UserDiskDataStore getInstance() {
    if (instance == null) {
      instance = new UserDiskDataStore();
    }
    return instance;
  }

  public Observable<AccountRealm> get(String userId) {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      AccountRealm account = realm.copyFromRealm(
          realm.where(AccountRealm.class)
              .equalTo(AccountRealmFields.ID, userId)
              .findFirst());

      realm.close();

      return account;
    });
  }

  public Observable<AccountRealm> getMe() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      AccountRealm account = realm.copyFromRealm(
          realm.where(AccountRealm.class)
              .equalTo(AccountRealmFields.ME, true)
              .findFirst());

      realm.close();

      return account;
    });
  }

  public void add(AccountRealm account) {
    addAll(Collections.singletonList(account));
  }

  public void addAll(List<AccountRealm> accounts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(accounts));
    realm.close();
  }

  public Completable delete(String userId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        AccountRealm account = tx.where(AccountRealm.class)
            .equalTo(AccountRealmFields.ID, userId)
            .findFirst();

        account.deleteFromRealm();
      });

      realm.close();
    });
  }

  public Observable<List<AccountRealm>> listRecentSearches() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      List<AccountRealm> accounts = realm.copyFromRealm(realm.where(AccountRealm.class)
          .equalTo(AccountRealmFields.RECENT, true)
          .findAll());

      realm.close();

      return accounts;
    });
  }
}
