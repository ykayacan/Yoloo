package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.db.PostRealm;

public class BlogItem implements FeedItem {
  private final PostRealm post;

  public BlogItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
