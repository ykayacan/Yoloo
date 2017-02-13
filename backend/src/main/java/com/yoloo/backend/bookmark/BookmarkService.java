package com.yoloo.backend.bookmark;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.post.Post;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class BookmarkService {

  public Bookmark create(Key<Post> questionKey, Key<Account> parentKey) {
    return Bookmark.builder()
        .id(questionKey.toWebSafeString())
        .parentUserKey(parentKey)
        .build();
  }
}
