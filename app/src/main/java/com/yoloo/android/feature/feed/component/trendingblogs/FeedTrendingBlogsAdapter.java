package com.yoloo.android.feature.feed.component.trendingblogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.widget.timeview.TimeTextView;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class FeedTrendingBlogsAdapter extends EpoxyAdapter {

  private final CropCircleTransformation circleTransformation =
      new CropCircleTransformation(YolooApp.getAppContext());
  private final RequestManager glide;

  private OnItemClickListener<PostRealm> onItemClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;

  private String userId;

  public FeedTrendingBlogsAdapter(RequestManager glide) {
    this.glide = glide;
  }

  void addTrendingBlogs(List<PostRealm> items) {
    addModels(createModels(items));
  }

  public void setOnItemClickListener(OnItemClickListener<PostRealm> listener) {
    this.onItemClickListener = listener;
  }

  public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
    this.onBookmarkClickListener = listener;
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener listener) {
    this.onPostOptionsClickListener = listener;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  private List<FeedTrendingBlogsAdapter$SubFeedTrendingBlogModel_> createModels(
      List<PostRealm> items) {
    if (onItemClickListener == null) {
      throw new IllegalStateException("onItemClickListener is null.");
    }

    if (userId == null) {
      throw new IllegalStateException("userId is null.");
    }

    return Stream.of(items)
        .map(posts -> new FeedTrendingBlogsAdapter$SubFeedTrendingBlogModel_().post(posts)
            .userId(userId)
            .glide(glide)
            .circleTransformation(circleTransformation)
            .onItemClickListener(onItemClickListener)
            .onPostOptionsClickListener(onPostOptionsClickListener)
            .onBookmarkClickListener(onBookmarkClickListener))
        .toList();
  }

  @EpoxyModelClass(layout = R.layout.item_feed_blog2)
  static abstract class SubFeedTrendingBlogModel
      extends EpoxyModelWithHolder<SubFeedTrendingBlogModel.SubFeedTrendingBlogHolder> {

    @EpoxyAttribute PostRealm post;
    @EpoxyAttribute String userId;
    @EpoxyAttribute(hash = false) RequestManager glide;
    @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;
    @EpoxyAttribute(hash = false) OnItemClickListener<PostRealm> onItemClickListener;
    @EpoxyAttribute(hash = false) OnBookmarkClickListener onBookmarkClickListener;
    @EpoxyAttribute(hash = false) OnPostOptionsClickListener onPostOptionsClickListener;

    @Override public void bind(SubFeedTrendingBlogHolder holder) {
      final Context context = holder.itemView.getContext();

      glide.load(post.getAvatarUrl())
          .bitmapTransform(circleTransformation)
          .placeholder(R.drawable.ic_player_72dp)
          .into(holder.ivUserAvatar);

      holder.tvUsername.setText(post.getUsername());
      holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
      holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
      holder.tvBounty.setText(String.valueOf(post.getBounty()));

      glide.load(post.getMedias().get(0).getThumbSizeUrl())
          .asBitmap()
          .override(100, 100)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .centerCrop()
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                GlideAnimation<? super Bitmap> glideAnimation) {
              RoundedBitmapDrawable rbd =
                  RoundedBitmapDrawableFactory.create(context.getResources(), resource);
              rbd.setCornerRadius(6f);
              holder.ivBlogBg.setImageDrawable(rbd);
            }
          });

      holder.tvBlogTitle.setText(post.getTitle());
      holder.tvBlogContent.setText(post.getContent());

      final int drawableIconRes =
          isSelf() ? R.drawable.ic_more_vert_black_24dp : R.drawable.ic_bookmark_black_24dp;
      holder.ibOptions.setImageDrawable(AppCompatResources.getDrawable(context, drawableIconRes));

      holder.ibOptions.setOnClickListener(v -> {
        if (isSelf()) {
          onPostOptionsClickListener.onPostOptionsClick(v, this, post);
        } else {
          final boolean isBookmarked = holder.ibOptions.getTag() == Boolean.TRUE;
          final int colorRes = isBookmarked ? android.R.color.secondary_text_dark : R.color.primary;

          holder.ibOptions.setTag(!isBookmarked);
          holder.ibOptions.setColorFilter(ContextCompat.getColor(context, colorRes),
              PorterDuff.Mode.SRC_IN);
          onBookmarkClickListener.onBookmarkClick(post.getId(), isBookmarked);
        }
      });

      holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, post));
    }

    @Override public void unbind(SubFeedTrendingBlogHolder holder) {
      Glide.clear(holder.ivBlogBg);
      holder.ivBlogBg.setImageDrawable(null);
    }

    boolean isSelf() {
      return post.getOwnerId().equals(userId);
    }

    static class SubFeedTrendingBlogHolder extends BaseEpoxyHolder {
      @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
      @BindView(R.id.tv_item_feed_username) TextView tvUsername;
      @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
      @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
      @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
      @BindView(R.id.iv_item_blog_cover) ImageView ivBlogBg;
      @BindView(R.id.tv_item_blog_title) TextView tvBlogTitle;
      @BindView(R.id.tv_item_blog_content) TextView tvBlogContent;
    }
  }
}
