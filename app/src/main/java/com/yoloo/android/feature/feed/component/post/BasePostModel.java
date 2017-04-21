package com.yoloo.android.feature.feed.component.post;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

public abstract class BasePostModel<T extends EpoxyHolder> extends EpoxyModelWithHolder<T> {
  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute String userId;
  @EpoxyAttribute(hash = false) RequestManager glide;
  @EpoxyAttribute(hash = false) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(hash = false) OnShareClickListener onShareClickListener;
  @EpoxyAttribute(hash = false) OnCommentClickListener onCommentClickListener;
  @EpoxyAttribute(hash = false) OnItemClickListener<PostRealm> onItemClickListener;
  @EpoxyAttribute(hash = false) OnBookmarkClickListener onBookmarkClickListener;
  @EpoxyAttribute(hash = false) OnPostOptionsClickListener onPostOptionsClickListener;
  @EpoxyAttribute(hash = false) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;

  boolean isSelf() {
    return post.getOwnerId().equals(userId);
  }

  public String getItemId() {
    return post.getId();
  }

  protected boolean isNormal() {
    return getLayout() != getDetailLayoutRes();
  }

  protected abstract int getDetailLayoutRes();
}
