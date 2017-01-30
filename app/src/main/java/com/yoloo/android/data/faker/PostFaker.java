package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.CategoryRealmFields;
import com.yoloo.android.data.model.PostRealm;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PostFaker {

  private static final int TYPE_NORMAL = 0;
  private static final int TYPE_RICH = 1;

  public static void generate() {
    Realm realm = Realm.getDefaultInstance();
    realm.executeTransactionAsync(tx -> {
      CategoryRealm category = tx.where(CategoryRealm.class)
          .equalTo(CategoryRealmFields.NAME, "Activities")
          .findFirst();

      RealmList<CategoryRealm> categories = new RealmList<>();
      categories.add(category);

      Set<PostRealm> list = new HashSet<>();

      PostRealm p1 = new PostRealm()
          .setId("p1")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("yasinsinankayacan")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(TYPE_NORMAL)
          .setCategories(categories)
          .setAcceptedCommentId("c1")
          .setBounty(20)
          .setFeedItem(true);

      PostRealm p2 = new PostRealm()
          .setId("p2")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("yasinsinankayacan")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(TYPE_NORMAL)
          .setAcceptedCommentId("c1")
          .setFeedItem(true);

      PostRealm p3 = new PostRealm()
          .setId("p3")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(TYPE_NORMAL)
          .setAcceptedCommentId("c1")
          .setFeedItem(false);

      PostRealm p4 = new PostRealm()
          .setId("p4")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setType(TYPE_RICH)
          .setAcceptedCommentId("c1")
          .setFeedItem(true);

      PostRealm p5 = new PostRealm()
          .setId("p5")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setAcceptedCommentId("c1")
          .setType(TYPE_RICH)
          .setFeedItem(true);

      PostRealm p6 = new PostRealm()
          .setId("p6")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setAcceptedCommentId("c1")
          .setType(TYPE_RICH)
          .setFeedItem(false);

      PostRealm p7 = new PostRealm()
          .setId("p7")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setAcceptedCommentId("c1")
          .setCategories(categories)
          .setType(TYPE_RICH)
          .setFeedItem(false);

      PostRealm p8 = new PostRealm()
          .setId("p8")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setAcceptedCommentId("c1")
          .setType(TYPE_RICH)
          .setCategories(categories)
          .setFeedItem(false);

      PostRealm p9 = new PostRealm()
          .setId("p9")
          .setOwnerId("a1")
          .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
          .setUsername("duygukeskek")
          .setCreated(FakerUtil.getRandomDate())
          .setContent(FakerUtil.getContent())
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setMediaUrl(FakerUtil.getMediaUrl())
          .setAcceptedCommentId("c1")
          .setType(TYPE_RICH)
          .setBookmarked(true)
          .setCategories(categories)
          .setFeedItem(false);

      list.add(p1);
      list.add(p2);
      list.add(p3);
      list.add(p4);
      list.add(p5);
      list.add(p6);
      list.add(p7);
      list.add(p8);
      list.add(p9);

      tx.insertOrUpdate(list);
    });

    realm.close();
  }

  public static PostRealm generateOne() {
    return new PostRealm()
        .setId(UUID.randomUUID().toString())
        .setOwnerId("a1")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
        .setUsername("yasinsinankayacan")
        .setCreated(FakerUtil.getRandomDate())
        .setContent(FakerUtil.getContent())
        .setComments(FakerUtil.generateNumber())
        .setVotes(FakerUtil.generateNumber())
        .setType(TYPE_NORMAL)
        .setBounty(20)
        .setFeedItem(true);
  }
}