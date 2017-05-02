package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.PostRealm;

public class RichQuestionItem implements FeedItem {
  private final PostRealm post;

  public RichQuestionItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
