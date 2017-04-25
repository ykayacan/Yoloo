package com.yoloo.backend.bookmark;

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
import lombok.experimental.NonFinal;
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Bookmark {

  public static final String FIELD_CREATED = "created";

  /**
   * websafe post id.
   */
  @Id
  private String id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  @Index
  @NonFinal
  private DateTime created;

  public static Key<Bookmark> createKey(Key<Account> accountKey, Key<PostEntity> postKey) {
    return Key.create(accountKey, Bookmark.class, postKey.toWebSafeString());
  }

  public Key<Bookmark> getKey() {
    return Key.create(parent, Bookmark.class, id);
  }

  public Key<PostEntity> getSavedPostKey() {
    return Key.create(id);
  }
}
