package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.FeedItem;

public class CommentFeedItem implements FeedItem {

  private final CommentRealm comment;

  public CommentFeedItem(CommentRealm comment) {
    this.comment = comment;
  }

  public CommentRealm getComment() {
    return comment;
  }
}
