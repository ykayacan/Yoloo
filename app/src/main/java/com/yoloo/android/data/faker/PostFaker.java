package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.util.Pair;
import io.realm.Realm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PostFaker {

  private static final int TYPE_NORMAL = 0;
  private static final int TYPE_RICH = 1;

  private static final List<Integer> TYPES = Arrays.asList(TYPE_NORMAL, TYPE_RICH);

  public static List<PostRealm> generateAll() {
    Random random = new Random();

    List<PostRealm> posts = new ArrayList<>();

    for (int i = 0; i < 20; i++) {
      Pair<String, String> pair =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));

      String content = PostContent.CONTENTS.get(random.nextInt(PostContent.CONTENTS.size()));
      int type = random.nextInt(TYPES.size());

      PostRealm post = new PostRealm()
          .setId("p" + i)
          .setOwnerId(pair.first)
          .setAvatarUrl(pair.second)
          .setUsername(pair.first)
          .setCreated(FakerUtil.getRandomDate())
          .setContent(content)
          .setCommentCount(FakerUtil.generateNumber())
          .setVoteCount(FakerUtil.generateNumber())
          .setPostType(type)
          .setBounty(random.nextInt(5))
          .setMediaUrl(type == 1
              ? "https://s-media-cache-ak0.pinimg.com/564x/41/94/11/419411f12cf09442a6e4f4797127209a.jpg"
              : null)
          .setPending(false)
          .setFeedItem(true);

      posts.add(post);
    }

    return posts;
  }

  public static PostRealm generateOne() {
    return new PostRealm()
        .setId(UUID.randomUUID().toString())
        .setOwnerId("a1")
        .setAvatarUrl(FakerUtil.getAvatarRandomUrl())
        .setUsername("yasinsinankayacan")
        .setCreated(FakerUtil.getRandomDate())
        .setContent(FakerUtil.getContent())
        .setCommentCount(FakerUtil.generateNumber())
        .setVoteCount(FakerUtil.generateNumber())
        .setPostType(TYPE_NORMAL)
        .setBounty(20)
        .setFeedItem(true);
  }

  public static void fakePosts() {
    Random random = new Random();

    Realm realm = Realm.getDefaultInstance();

    realm.executeTransaction(tx -> {
      TagRealm haydarpasa = new TagRealm()
          .setId("haydarpasa")
          .setName("haydarpasa")
          .setPosts(3)
          .setRecent(true);

      TagRealm sultanahmet = new TagRealm()
          .setId("sultanahmet")
          .setName("sultanahmet")
          .setPosts(4)
          .setRecent(true);

      TagRealm ayasofya = new TagRealm()
          .setId("ayasofya")
          .setName("ayasofya")
          .setPosts(1)
          .setRecent(true);

      TagRealm gülhane = new TagRealm()
          .setId("gülhane")
          .setName("gülhane")
          .setPosts(4)
          .setRecent(true);

      TagRealm cadır = new TagRealm()
          .setId("çadır")
          .setName("çadır")
          .setPosts(4)
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
          .setPostType(PostRealm.POST_TEXT)
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
          .setPostType(PostRealm.POST_TEXT)
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
          .setPostType(PostRealm.POST_TEXT)
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
          .setPostType(PostRealm.POST_TEXT)
          .addTag(sultanahmet)
          .setBounty(0)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p4);

      Pair<String, String> pair5 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));

      PostRealm p5 = new PostRealm()
          .setId("p5")
          .setOwnerId(pair5.first)
          .setAvatarUrl(pair5.second)
          .setUsername(pair5.first)
          .setContent(PostContent.CONTENTS.get(8))
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.POST_RICH)
          .setMediaUrl("http://www.adrenalinoutdoor.com/images_buyuk/f62/"
              + "The-North-Face-Mountain-25-Cadir_16762_2.jpg")
          .addTag(cadır)
          .setBounty(5)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p5);

      Pair<String, String> pair6 =
          AccountFaker.NAME_PAIRS.get(random.nextInt(AccountFaker.NAME_PAIRS.size()));

      PostRealm p6 = new PostRealm()
          .setId("p6")
          .setOwnerId(pair6.first)
          .setAvatarUrl(pair6.second)
          .setUsername(pair6.first)
          .setTitle("Sultanahmet'te Mutlaka Görülmesi Gereken Yerler")
          .setContent(PostContent.CONTENTS2.get(3))
          .setCreated(FakerUtil.getRandomDate())
          .setVoteCount(FakerUtil.generateNumber())
          .setCommentCount(FakerUtil.generateNumber())
          .setPostType(PostRealm.POST_BLOG)
          .setMediaUrl("https://supergezginler.com/wp-content/uploads/"
              + "sultanahmet-gezilecek-yerler-6.jpg")
          .addTag(sultanahmet)
          .addTag(ayasofya)
          .setPending(false)
          .setFeedItem(true);

      posts.add(p6);

      tx.insertOrUpdate(posts);
    });

    realm.close();
  }
}