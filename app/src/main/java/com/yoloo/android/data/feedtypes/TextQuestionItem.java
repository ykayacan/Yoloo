package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.db.PostRealm;

public class TextQuestionItem implements FeedItem {
  private final PostRealm post;

  public TextQuestionItem(PostRealm post) {
    this.post = post;
  }

  public PostRealm getPost() {
    return post;
  }
}
