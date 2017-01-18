package com.yoloo.android.data.repository.user.datasource;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.AccountRealmFields;
import io.reactivex.Single;
import io.realm.Realm;

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
}