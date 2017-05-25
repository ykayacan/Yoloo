package com.yoloo.backend.feed;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.post.PostEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
public class Feed {

  public static final String FIELD_POST = "post";
  public static final String FIELD_CREATED = "created";

  // feed:postWebsafeId
  @Id private String id;

  @Parent private Key<Account> parent;

  @Index private DateTime created;

  public static String createId(Key<PostEntity> postKey) {
    return "feed:" + postKey.toWebSafeString();
  }

  public static Key<Feed> createKey(Key<PostEntity> postKey, Key<Account> parentKey) {
    return Key.create(parentKey, Feed.class, "feed:" + postKey.toWebSafeString());
  }

  public static Key<PostEntity> getPostKey(Key<Feed> feedKey) {
    return Key.create(feedKey.getName().split(":")[1]);
  }
}
