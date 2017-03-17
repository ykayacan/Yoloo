package com.yoloo.backend.feed;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.post.Post;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Feed {

  // feed:postWebsafeId
  @Id
  private String id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  public static String createId(Key<Post> postKey) {
    return "feed:" + postKey.toWebSafeString();
  }

  public static Key<Feed> createKey(Key<Post> postKey, Key<Account> parentKey) {
    return Key.create(parentKey, Feed.class, postKey.toWebSafeString());
  }

  public static Key<Post> getPostKey(Key<Feed> feedKey) {
    final String name = feedKey.getName();
    return Key.create(name.substring(name.indexOf(":")));
  }
}
