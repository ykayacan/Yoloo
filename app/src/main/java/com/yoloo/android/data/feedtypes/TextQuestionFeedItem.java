package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.PostRealm;

public class TextQuestionFeedItem implements FeedItem {
  private final PostRealm post;

  public TextQuestionFeedItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
