package com.yoloo.android.feature.blog.models;

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
}
