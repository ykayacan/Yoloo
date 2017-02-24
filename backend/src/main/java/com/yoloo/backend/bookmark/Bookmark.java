package com.yoloo.backend.bookmark;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.post.Post;
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
   * websafe question id.
   */
  @Id
  private String id;

  @Parent
  @NonFinal
  private Key<Account> parent;

  @Index
  @NonFinal
  private DateTime created;

  public Key<Post> getSavedPostKey() {
    return Key.create(id);
  }
}
