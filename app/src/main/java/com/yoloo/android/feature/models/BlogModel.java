package com.yoloo.android.feature.models;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.ui.widget.BlogView;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_blog)
public abstract class BlogModel extends EpoxyModel<BlogView> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute(DoNotHash) PostCallbacks callbacks;

  @Override
  public void bind(BlogView view) {
    super.bind(view);
    view.setPost(post);
    view.setBlogTitle(post.getTitle());
    view.setBlogUserInfo(post.getUsername(), "");
    view.setBlogContent(post.getContent());
    view.setPostCallbacks(callbacks, post);
  }
}
