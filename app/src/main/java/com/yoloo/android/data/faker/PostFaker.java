package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.util.Pair;
import io.realm.Realm;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostFaker {

  public static void fakePosts() {
    Random random = new Random();

    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      TagRealm haydarpasa = new TagRealm()
          .setId("haydarpasa")
          .setName("haydarpasa")
          .setPostCount(3)
          .setRecent(true);

      TagRealm sultanahmet = new TagRealm()
          .setId("sultanahmet")
          .setName("sultanahmet")
          .setPostCount(4)
          .setRecent(true);

      TagRealm ayasofya = new TagRealm()
          .setId("ayasofya")
          .setName("ayasofya")
          .setPostCount(1)
          .setRecent(true);

      TagRealm gülhane = new TagRealm()
          .setId("gülhane")
          .setName("gülhane")
          .setPostCount(4)
          .setRecent(true);

      TagRealm cadır = new TagRealm()
          .setId("çadır")
          .setName("çadır")
          .setPostCount(4)
          .setRecent(true);

      List<TagRealm> tags = new ArrayList<>();
      tags.add(haydarpasa);
      tags.add(sultanahmet);
      tags.add(ayasofya);
      tags.add(gülhane);
      tags.add(cadır);

      tx.insertOrUpdate(tags);

      List<PostRealm> posts = new ArrayList<>();

      Pair<String, String> pair1 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));
      String content1 = PostContent.CONTENTS.get(random.nextInt(PostContent.CONTENTS.size()));

      PostRealm p1 = new PostRealm()
          .setId("p1")
          .setOwnerId(pair1.first)
          .setAvatarUrl(pair1.second)
          .setUsername(pair1.first)
          .setContent(content1)
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.TYPE_TEXT)
          .setBounty(random.nextInt(5))
          .setPending(false)
          .setFeedItem(true);

      posts.add(p1);

      Pair<String, String> pair2 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));
      String content2 = PostContent.CONTENTS.get(random.nextInt(PostContent.CONTENTS.size()));

      PostRealm p2 = new PostRealm()
          .setId("p2")
          .setOwnerId(pair2.first)
          .setAvatarUrl(pair2.second)
          .setUsername(pair2.first)
          .setContent(content2)
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.TYPE_TEXT)
          .setBounty(0)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p2);

      Pair<String, String> pair3 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));
      String content3 = PostContent.CONTENTS.get(random.nextInt(PostContent.CONTENTS.size()));

      PostRealm p3 = new PostRealm()
          .setId("p3")
          .setOwnerId(pair3.first)
          .setAvatarUrl(pair3.second)
          .setUsername(pair3.first)
          .setContent(content3)
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.TYPE_TEXT)
          .setBounty(0)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p3);

      Pair<String, String> pair4 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));

      PostRealm p4 = new PostRealm()
          .setId("p4")
          .setOwnerId(pair4.first)
          .setAvatarUrl(pair4.second)
          .setUsername(pair4.first)
          .setContent(PostContent.CONTENTS2.get(0))
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.TYPE_TEXT)
          .addTag(sultanahmet)
          .setBounty(0)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p4);

      Pair<String, String> pair5 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));

      /*PostRealm p5 = new PostRealm()
          .setId("p5")
          .setOwnerId(pair5.first)
          .setAvatarUrl(pair5.second)
          .setUsername(pair5.first)
          .setContent(PostContent.CONTENTS.get(8))
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.TYPE_RICH)
          .setMedias("http://www.adrenalinoutdoor.com/images_buyuk/f62/"
              + "The-North-Face-Mountain-25-Cadir_16762_2.jpg")
          .addTag(cadır)
          .setBounty(5)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p5);*/

      Pair<String, String> pair6 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));

      /*PostRealm p6 = new PostRealm()
          .setId("p6")
          .setOwnerId(pair6.first)
          .setAvatarUrl(pair6.second)
          .setUsername(pair6.first)
          .setTitle("Sultanahmet'te Mutlaka Görülmesi Gereken Yerler")
          .setContent(PostContent.CONTENTS2.get(3))
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.TYPE_BLOG)
          .setMedias("https://supergezginler.com/wp-content/uploads/"
              + "sultanahmet-gezilecek-yerler-6.jpg")
          .addTag(sultanahmet)
          .addTag(ayasofya)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p6);*/

      tx.insertOrUpdate(posts);
    });

    realm.close();
  }
}
