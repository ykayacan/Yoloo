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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommentItem)) return false;

    CommentItem that = (CommentItem) o;

    return comment != null ? comment.equals(that.comment) : that.comment == null;
  }

  @Override public int hashCode() {
    return comment != null ? comment.hashCode() : 0;
  }
}
