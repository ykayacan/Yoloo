package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.util.Pair;
import io.realm.Realm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AccountFaker {

  public static final List<Pair<String, String>> NAME_PAIRS = Arrays.asList(
      new Pair<>("evelynedwards", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("keith", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("muralerdil", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("eino", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("emreoz_", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("gezginn", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("danwright", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("nihal034", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("burak90", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("cemmm", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("ruzgartunceri", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("iremyilmazer", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("avanebru", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("sahnur43", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("burcu42", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("ruyacevik", FakerUtil.getFemaleAvatarUrl())
  );

  public static void generateAll() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      Random random = new Random();

      // Accounts
      List<AccountRealm> accounts = new ArrayList<>();
      for (int i = 0; i < 15; i++) {
        int index = random.nextInt(NAME_PAIRS.size());

        AccountRealm account = new AccountRealm()
            .setId(NAME_PAIRS.get(index).first)
            .setUsername(NAME_PAIRS.get(index).first)
            .setRealname(NAME_PAIRS.get(index).first)
            .setAvatarUrl(NAME_PAIRS.get(index).second)
            .setLevel(random.nextInt(4))
            .setBounties(random.nextInt(30))
            .setPosts(random.nextInt(40))
            .setFollowers(random.nextInt(100))
            .setFollowings(random.nextInt(100))
            .setAchievements(random.nextInt(5))
            .setPoints(random.nextInt(120))
            .setMe(false);

        accounts.add(account);
      }

      tx.insertOrUpdate(accounts);
    });

    realm.close();
  }

  public static AccountRealm generateMe() {
    return new AccountRealm()
        .setId("a1")
        .setUsername("krialix")
        .setLevel(2)
        .setRealname("Yasin Sinan Kayacan")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
        .setBounties(35)
        .setPosts(56)
        .setFollowers(123)
        .setFollowings(13)
        .setAchievements(3)
        .setPoints(88)
        .setMe(true);
  }

  public static AccountRealm generateOne() {
    Random random = new Random();

    int size = NAME_PAIRS.size();

    return new AccountRealm()
        .setId(UUID.randomUUID().toString())
        .setUsername(NAME_PAIRS.get(random.nextInt(size)).first)
        .setLevel(random.nextInt(4))
        .setRealname(NAME_PAIRS.get(random.nextInt(size)).first)
        .setAvatarUrl(NAME_PAIRS.get(random.nextInt(size)).second)
        .setBounties(random.nextInt(30))
        .setPosts(random.nextInt(40))
        .setFollowers(random.nextInt(100))
        .setFollowings(random.nextInt(100))
        .setAchievements(random.nextInt(5))
        .setPoints(random.nextInt(120))
        .setMe(false);
  }

  public static void setGirlUser() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      AccountRealm account = new AccountRealm()
          .setId("girl")
          .setUsername("bernabakır")
          .setRealname("Berna Bakır")
          .setAvatarUrl(
              "https://image.ibb.co/dHzfwF/berna.jpg")
          .setLevel(1)
          .setBounties(30)
          .setPosts(4)
          .setFollowers(12)
          .setFollowings(6)
          .setAchievements(1)
          .setPoints(25)
          .setMe(true);

      tx.insertOrUpdate(account);
    });

    realm.close();
  }

  public static void setMaleUser() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      AccountRealm account = new AccountRealm()
          .setId("male")
          .setUsername("yunusemrecan")
          .setRealname("Yunus Emre Can")
          .setAvatarUrl(
              "https://scontent-frt3-1.xx.fbcdn.net/v/t34.0-12/16839680_"
                  + "1158514590938754_637495211_n.jpg?oh=f1ca8d90ac7db9b77834"
                  + "f9f6cc74bd6a&oe=58AB309A")
          .setLevel(2)
          .setBounties(20)
          .setPosts(8)
          .setFollowers(5)
          .setFollowings(14)
          .setAchievements(1)
          .setPoints(20)
          .setMe(true);

      tx.insertOrUpdate(account);
    });

    realm.close();
  }
}
