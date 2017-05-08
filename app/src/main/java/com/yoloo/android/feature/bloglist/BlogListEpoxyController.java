package com.yoloo.android.feature.bloglist;

import android.content.Context;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class BlogListEpoxyController extends Typed2EpoxyController<List<PostRealm>, Boolean> {

  private final CropCircleTransformation circleTransformation;
  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  private String userId;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;
  private OnItemClickListener<PostRealm> onPostClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnVoteClickListener onVoteClickListener;

  private List<PostRealm> posts;

  BlogListEpoxyController(Context context, RequestManager glide) {
    this.glide = glide;
    this.circleTransformation = new CropCircleTransformation(context);
    this.posts = new ArrayList<>();
  }

  void setUserId(String userId) {
    this.userId = userId;
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

  @Override public void setData(List<PostRealm> posts, Boolean loadingMore) {
    this.posts = posts;
    super.setData(posts, loadingMore);
  }

  @Override
  protected void buildModels(List<PostRealm> posts, Boolean loadingMore) {
    Stream.of(posts).forEach(this::createBlog);

    loaderModel.addIf(loadingMore, this);
  }

  public void deletePost(PostRealm post) {
    posts.remove(post);
    setData(posts, false);
  }

  public void showLoader() {
    setData(posts, true);
  }

  public void hideLoader() {
    setData(posts, false);
  }

  private void createBlog(PostRealm post) {
    new BlogModel_()
        .id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .bitmapTransformation(circleTransformation)
        .glide(glide)
        .post(post)
        .addTo(this);
  }
}
