package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import io.realm.Realm;
import java.util.ArrayList;
import java.util.List;

public class CommentFaker {

  public static void generate() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      AccountRealm account = new AccountRealm();
      account.setAvatarUrl(FakerUtil.getAvatarRandomUrl());
      account.setUsername("krialix");

      tx.insertOrUpdate(account);

      List<CommentRealm> comments = new ArrayList<>();

      CommentRealm c1 = new CommentRealm()
          .setId("c1")
          .setOwnerId(account.getId())
          .setUsername(account.getUsername())
          .setAvatarUrl(account.getAvatarUrl())
          .setContent(FakerUtil.getContent())
          .setCreated(FakerUtil.getRandomDate())
          .setDir(1)
          .setVotes(FakerUtil.generateNumber())
          .setPostId("p1")
          .setAccepted(false);

      CommentRealm c2 = new CommentRealm()
          .setId("c2")
          .setOwnerId(account.getId())
          .setUsername(account.getUsername())
          .setAvatarUrl(account.getAvatarUrl())
          .setContent(FakerUtil.getContent())
          .setCreated(FakerUtil.getRandomDate())
          .setDir(-1)
          .setVotes(FakerUtil.generateNumber())
          .setPostId("p1")
          .setAccepted(false);

      CommentRealm c3 = new CommentRealm()
          .setId("c3")
          .setOwnerId(account.getId())
          .setUsername(account.getUsername())
          .setAvatarUrl(account.getAvatarUrl())
          .setContent(FakerUtil.getContent())
          .setCreated(FakerUtil.getRandomDate())
          .setDir(-1)
          .setVotes(FakerUtil.generateNumber())
          .setPostId("p1")
          .setAccepted(false);

      comments.add(c1);
      comments.add(c2);
      comments.add(c3);

      tx.insertOrUpdate(comments);
    });
    realm.close();
  }
}
