package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.PostRealm;

public class RichQuestionFeedItem implements FeedItem {
  private final PostRealm post;

  public RichQuestionFeedItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
