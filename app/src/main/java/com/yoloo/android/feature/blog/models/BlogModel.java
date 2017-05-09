package com.yoloo.android.feature.blog.models;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.widget.BlogView;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_blog)
public abstract class BlogModel extends EpoxyModel<BlogView> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute(DoNotHash) OnShareClickListener onShareClickListener;
  @EpoxyAttribute(DoNotHash) OnCommentClickListener onCommentClickListener;
  @EpoxyAttribute(DoNotHash) OnVoteClickListener onVoteClickListener;

  @Override
  public void bind(BlogView view) {
    super.bind(view);
    view.setPost(post);
    view.setBlogTitle(post.getTitle());
    view.setBlogUserInfo(post.getUsername(), "");
    view.setBlogContent(post.getContent());
    view.setOnCommentClickListener(onCommentClickListener, post);
    view.setOnShareClickListener(onShareClickListener, post);
    view.setOnVoteClickListener(onVoteClickListener, post);
  }
}
