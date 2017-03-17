package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.PostRealm;

public class BlogFeedItem implements FeedItem {
  private final PostRealm post;

  public BlogFeedItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
