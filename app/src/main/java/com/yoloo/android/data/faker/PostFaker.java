package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.CategoryRealmFields;
import com.yoloo.android.data.model.PostRealm;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.HashSet;
import java.util.Set;

public class PostFaker {

  private static final int TYPE_NORMAL = 0;
  private static final int TYPE_RICH = 1;

  public static void generate() {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransaction(tx -> {
      CategoryRealm category =
          tx.where(CategoryRealm.class).equalTo(CategoryRealmFields.NAME, "Activities").findFirst();

      RealmList<CategoryRealm> categories = new RealmList<>();
      categories.add(category);

      Set<PostRealm> list = new HashSet<>();

      PostRealm p1 = new PostRealm()
          .setId("p1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("yasinsinankayacan")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(TYPE_NORMAL)
          .setCategories(categories)
          .setFeedItem(true);

      PostRealm p2 = new PostRealm()
          .setId("p2")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("yasinsinankayacan")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(TYPE_NORMAL)
          .setFeedItem(true);

      PostRealm p3 = new PostRealm()
          .setId("p3")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(TYPE_NORMAL)
          .setFeedItem(false);

      PostRealm p4 = new PostRealm()
          .setId("p4")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setType(TYPE_RICH)
          .setFeedItem(true);

      PostRealm p5 = new PostRealm()
          .setId("p5")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setType(TYPE_RICH)
          .setFeedItem(true);

      PostRealm p6 = new PostRealm()
          .setId("p6")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setType(TYPE_RICH)
          .setFeedItem(false);

      list.add(p1);
      list.add(p2);
      list.add(p3);
      list.add(p4);
      list.add(p5);
      list.add(p6);

      tx.insertOrUpdate(list);
    });

    realm.close();
  }
}