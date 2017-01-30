package com.yoloo.android.data.repository.user.datasource;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import java.util.Collections;
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

  /**
   * Get observable.
   *
   * @param userId the user id
   * @return the observable
   */
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

  /**
   * Gets me.
   *
   * @return the me
   */
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

  /**
   * Add completable.
   *
   * @param account the account
   * @return the completable
   */
  public void add(AccountRealm account) {
    addAll(Collections.singletonList(account));
  }

  /**
   * Add all completable.
   *
   * @param accounts the accounts
   * @return the completable
   */
  public void addAll(List<AccountRealm> accounts) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> tx.insertOrUpdate(accounts));
    realm.close();
  }

  /**
   * Delete completable.
   *
   * @param userId the user id
   * @return the completable
   */
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

  /**
   * List recent observable.
   *
   * @return the observable
   */
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