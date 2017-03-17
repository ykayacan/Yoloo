package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.util.Pair;
import io.realm.Realm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AppFaker {

  private static final int TYPE_NORMAL = 0;
  private static final int TYPE_RICH = 1;

  public static final List<Integer> TYPES = Arrays.asList(TYPE_NORMAL, TYPE_RICH);

  private static final List<Pair<String, String>> NAME_PAIRS = Arrays.asList(
      new Pair<>("evelynedwards", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("keith", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("antonie", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("eino", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("terry", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("kapser", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("danwright", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("nihal034", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("pnuts", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("guymoreno7kj", FakerUtil.getMaleAvatarUrl()),
      new Pair<>("ruzgartunceri", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("iremyilmazer", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("avanebru", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("avanebru", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("sahnur43", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("burcu42", FakerUtil.getFemaleAvatarUrl()),
      new Pair<>("ruyacevik", FakerUtil.getFemaleAvatarUrl())
  );

  public static void populateDatabase() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      Random random = new Random();

      // Accounts
      List<AccountRealm> accounts = new ArrayList<>();
      for (int i = 0; i < 15; i++) {
        int index = random.nextInt(NAME_PAIRS.size());

        AccountRealm account = new AccountRealm()
            .setId(UUID.randomUUID().toString())
            .setUsername(NAME_PAIRS.get(index).first)
            .setRealname(NAME_PAIRS.get(index).first)
            .setAvatarUrl(NAME_PAIRS.get(index).second)
            .setLevel(random.nextInt(4))
            .setBountyCount(random.nextInt(30))
            .setPostCount(random.nextInt(40))
            .setFollowerCount(random.nextInt(100))
            .setFollowingCount(random.nextInt(100))
            .setAchievementCount(random.nextInt(5))
            .setPointCount(random.nextInt(120))
            .setMe(false);

        accounts.add(account);
      }

      tx.insertOrUpdate(accounts);
    });

    realm.close();
  }
}
