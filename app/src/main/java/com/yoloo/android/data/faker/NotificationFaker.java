package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.NotificationRealm;
import io.realm.Realm;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NotificationFaker {

  public static void generate() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      Set<NotificationRealm> set = new HashSet<>();

      NotificationRealm n1 = new NotificationRealm()
          .setId("n1")
          .setSenderId(UUID.randomUUID().toString())
          .setAction(NotificationRealm.FOLLOW)
          .setSenderAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setSenderUsername("krialix")
          .setCreated(new Date());

      NotificationRealm n2 = new NotificationRealm()
          .setId("n2")
          .setSenderId(UUID.randomUUID().toString())
          .setAction(NotificationRealm.COMMENT)
          .setSenderAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setSenderUsername("krialix")
          .setMessage("Merhaba bu yeni bir mesaj")
          .setCreated(new Date());

      NotificationRealm n3 = new NotificationRealm()
          .setId("n3")
          .setSenderId(UUID.randomUUID().toString())
          .setAction(NotificationRealm.MENTION)
          .setSenderAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setSenderUsername("krialix")
          .setMessage("seni mention ettim")
          .setCreated(new Date());

      set.add(n1);
      set.add(n2);
      set.add(n3);

      tx.insertOrUpdate(set);
    });

    realm.close();
  }
}
