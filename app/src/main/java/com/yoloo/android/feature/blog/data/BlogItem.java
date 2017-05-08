package com.yoloo.android.feature.blog.data;

import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.util.EpoxyItem;

public class BlogItem implements EpoxyItem<PostRealm> {

  private final PostRealm post;

  public BlogItem(PostRealm post) {
    this.post = post;
  }

  @Override
  public PostRealm getItem() {
    return post;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BlogItem)) return false;

    BlogItem blogItem = (BlogItem) o;

    return post != null ? post.equals(blogItem.post) : blogItem.post == null;
  }

  @Override public int hashCode() {
    return post != null ? post.hashCode() : 0;
  }
}
