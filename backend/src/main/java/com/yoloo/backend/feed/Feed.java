package com.yoloo.backend.feed;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.post.Post;
import lombok.Builder;
import lombok.Value;

@Entity
@Cache
@Value
@Builder
public class Feed {

  @Id
  private Long id;

  @Parent
  private Key<Account> parent;

  @Load
  private Ref<Post> postRef;

  public Post getFeedItem() {
    return postRef.getValue();
  }
}
