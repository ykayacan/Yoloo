package com.yoloo.android.feature.blog;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.post.BlogModel;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class BlogListAdapter extends EpoxyAdapter {

  private final CropCircleTransformation circleTransformation;
  private final RequestManager glide;

  private String userId;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;
  private OnItemClickListener<PostRealm> onPostClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnVoteClickListener onVoteClickListener;

  BlogListAdapter(Context context, RequestManager glide) {
    this.glide = glide;
    enableDiffing();
    circleTransformation = new CropCircleTransformation(context);
  }

  void setUserId(String userId) {
    this.userId = userId;
  }

  void addBlogs(List<PostRealm> blogs) {
    for (PostRealm post : blogs) {
      models.add(createBlog(post));
    }

    notifyModelsChanged();
  }

  public void setOnProfileClickListener(OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener onPostOptionsClickListener) {
    this.onPostOptionsClickListener = onPostOptionsClickListener;
  }

  public void setOnBookmarkClickListener(OnBookmarkClickListener onBookmarkClickListener) {
    this.onBookmarkClickListener = onBookmarkClickListener;
  }

  public void setOnPostClickListener(OnItemClickListener<PostRealm> onPostClickListener) {
    this.onPostClickListener = onPostClickListener;
  }

  public void setOnShareClickListener(OnShareClickListener onShareClickListener) {
    this.onShareClickListener = onShareClickListener;
  }

  public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener) {
    this.onCommentClickListener = onCommentClickListener;
  }

  public void setOnVoteClickListener(OnVoteClickListener onVoteClickListener) {
    this.onVoteClickListener = onVoteClickListener;
  }

  private BlogModel createBlog(PostRealm post) {
    return new BlogModel_().id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .post(post);
  }
}
