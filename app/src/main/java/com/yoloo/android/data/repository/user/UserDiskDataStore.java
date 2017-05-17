package com.yoloo.android.data.repository.user;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.AccountRealmFields;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

class UserDiskDataStore {

  private static UserDiskDataStore instance;

  private UserDiskDataStore() {
  }

  static UserDiskDataStore getInstance() {
    if (instance == null) {
      instance = new UserDiskDataStore();
    }
    return instance;
  }

  Single<Optional<AccountRealm>> get(@Nonnull String userId) {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      AccountRealm account;

      AccountRealm result =
          realm.where(AccountRealm.class).equalTo(AccountRealmFields.ID, userId).findFirst();

      account = result == null ? null : realm.copyFromRealm(result);

      realm.close();

      return Optional.ofNullable(account);
    });
  }

  Single<Optional<AccountRealm>> getMe() {
    return Single.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      AccountRealm result =
          realm.where(AccountRealm.class).equalTo(AccountRealmFields.ME, true).findFirst();

      Optional<AccountRealm> account =
          result == null ? Optional.empty() : Optional.of(realm.copyFromRealm(result));

      realm.close();

      return account;
    });
  }

  void add(@Nonnull AccountRealm account) {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> {
      // Clear previous user account.
      if (account.isMe()) {
        AccountRealm me =
            tx.where(AccountRealm.class).equalTo(AccountRealmFields.ME, true).findFirst();

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

  Completable delete(@Nonnull String userId) {
    return Completable.fromAction(() -> {
      Realm realm = Realm.getDefaultInstance();

      realm.executeTransaction(tx -> {
        AccountRealm account =
            tx.where(AccountRealm.class).equalTo(AccountRealmFields.ID, userId).findFirst();

        account.deleteFromRealm();
      });

      realm.close();
    });
  }

  void addRecentSearchedAccounts(List<AccountRealm> accounts) {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      long recentCount =
          tx.where(AccountRealm.class).equalTo(AccountRealmFields.RECENT, true).count();

      if (recentCount < 10) {
        tx.insertOrUpdate(Stream
            .of(accounts)
            .map(account -> account.setRecent(true))
            .limit(10 - recentCount)
            .toList());
      } else {
        // clear all recents
        tx.where(AccountRealm.class)
            .equalTo(AccountRealmFields.RECENT, true)
            .findAll()
            .deleteAllFromRealm();

        tx.insertOrUpdate(Stream
            .of(accounts)
            .map(account -> account.setRecent(true))
            .limit(10)
            .toList());
      }
    });

    realm.close();
  }

  Observable<List<AccountRealm>> listRecentSearches() {
    return Observable.fromCallable(() -> {
      Realm realm = Realm.getDefaultInstance();

      RealmResults<AccountRealm> results =
          realm.where(AccountRealm.class).equalTo(AccountRealmFields.RECENT, true).findAll();

      if (results.isEmpty()) {
        realm.close();
        return Collections.emptyList();
      } else {
        List<AccountRealm> accounts = realm.copyFromRealm(results);
        realm.close();
        return accounts;
      }
    });
  }
}
