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
          .setLevel(2)
          .setRealname("Yasin Sinan Kayacan")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setBounties(35)
          .setQuestions(56)
          .setFollowers(123)
          .setFollowings(13)
          .setAchievements(3)
          .setPoints(88)
          .setMe(true);

      tx.insertOrUpdate(account);
    });

    realm.close();
  }

  public static AccountRealm generateOne() {
    return new AccountRealm()
        .setId("a1")
        .setUsername("krialix")
        .setLevel(2)
        .setRealname("Yasin Sinan Kayacan")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
        .setBounties(35)
        .setQuestions(56)
        .setFollowers(123)
        .setFollowings(13)
        .setAchievements(3)
        .setPoints(88)
        .setMe(true);
  }
}
