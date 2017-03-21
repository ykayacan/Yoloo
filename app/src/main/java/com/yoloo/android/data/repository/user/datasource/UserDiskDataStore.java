package com.yoloo.android.data.repository.user.datasource;

import com.annimon.stream.Optional;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;

import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.Sort;

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

  public Single<Optional<AccountRealm>> get(@Nonnull String userId) {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      AccountRealm account;

      AccountRealm result = realm.where(AccountRealm.class)
          .equalTo(AccountRealmFields.ID, userId)
          .findFirst();

      account = result == null ? null : realm.copyFromRealm(result);

      realm.close();

      return Optional.ofNullable(account);
    });
  }

  public Single<Optional<AccountRealm>> getMe() {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      AccountRealm result = realm.where(AccountRealm.class)
              .equalTo(AccountRealmFields.ME, true)
              .findFirst();

      Optional<AccountRealm> account = result == null
          ? Optional.empty()
          : Optional.of(realm.copyFromRealm(result));

      realm.close();

      return account;
    });
  }

  public void add(@Nonnull AccountRealm account) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> {
      // Clear previous user account.
      if (account.isMe()) {
        AccountRealm me = tx.where(AccountRealm.class)
            .equalTo(AccountRealmFields.ME, true)
            .findFirst();

        if (me != null) {
          me.deleteFromRealm();
        }
      } else {
        final int size = tx.where(AccountRealm.class).findAll().size();

        if (size == 10) {
          AccountRealm oldest = tx.where(AccountRealm.class)
              .notEqualTo(AccountRealmFields.ME, true)
              .findAllSorted(AccountRealmFields.LOCAL_SAVE_DATE, Sort.ASCENDING)
              .first();

          if (oldest != null) {
            oldest.deleteFromRealm();
          }
        }
      }

      tx.insertOrUpdate(account);
    });
    realm.close();
  }

  public Completable delete(@Nonnull String userId) {
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
