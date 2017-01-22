package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.AccountRealm;
import io.realm.Realm;

public class AccountFaker {

  public static void generate() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      AccountRealm account = new AccountRealm()
          .setId("a1")
          .setUsername("krialix")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setBounties(35)
          .setMe(true);

      tx.insertOrUpdate(account);
    });

    realm.close();
  }
}
