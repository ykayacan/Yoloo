package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.model.FeedItem;
import com.yoloo.android.data.model.PostRealm;

public class PostFeedItem implements FeedItem {

  private final PostRealm post;

  public PostFeedItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
