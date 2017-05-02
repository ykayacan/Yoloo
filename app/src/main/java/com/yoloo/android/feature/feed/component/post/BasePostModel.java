package com.yoloo.android.feature.feed.component.post;

import android.graphics.Bitmap;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

public abstract class BasePostModel<T extends EpoxyHolder> extends EpoxyModelWithHolder<T> {
  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute String userId;
  @EpoxyAttribute(DoNotHash) RequestManager glide;
  @EpoxyAttribute(DoNotHash) OnProfileClickListener onProfileClickListener;
  @EpoxyAttribute(DoNotHash) OnShareClickListener onShareClickListener;
  @EpoxyAttribute(DoNotHash) OnCommentClickListener onCommentClickListener;
  @EpoxyAttribute(DoNotHash) OnItemClickListener<PostRealm> onItemClickListener;
  @EpoxyAttribute(DoNotHash) OnBookmarkClickListener onBookmarkClickListener;
  @EpoxyAttribute(DoNotHash) OnPostOptionsClickListener onPostOptionsClickListener;
  @EpoxyAttribute(DoNotHash) OnVoteClickListener onVoteClickListener;
  @EpoxyAttribute(DoNotHash) Transformation<Bitmap> bitmapTransformation;

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
