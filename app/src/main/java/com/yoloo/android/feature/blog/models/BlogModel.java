package com.yoloo.android.feature.blog.models;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.ui.widget.BlogView;

@EpoxyModelClass(layout = R.layout.item_blog)
public abstract class BlogModel extends EpoxyModel<BlogView> {

  @EpoxyAttribute PostRealm post;

  @Override
  public void bind(BlogView view) {
    super.bind(view);
    view.setBlogTitle(post.getTitle());
    view.setBlogUserInfo(post.getUsername(), "");
    view.setBlogContent(post.getContent());
  }
}
