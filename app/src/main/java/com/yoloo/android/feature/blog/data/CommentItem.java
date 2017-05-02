package com.yoloo.android.feature.blog.data;

import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.util.EpoxyItem;

public class CommentItem implements EpoxyItem<CommentRealm> {

  private final CommentRealm comment;

  public CommentItem(CommentRealm comment) {
    this.comment = comment;
  }

  @Override
  public CommentRealm getItem() {
    return comment;
  }
}
