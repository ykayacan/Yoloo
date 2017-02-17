package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.util.Pair;
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
          .setComments(FakerUtil.generateNumber())
          .setVotes(FakerUtil.generateNumber())
          .setType(type)
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
        .setComments(FakerUtil.generateNumber())
        .setVotes(FakerUtil.generateNumber())
        .setType(TYPE_NORMAL)
        .setBounty(20)
        .setFeedItem(true);
  }
}