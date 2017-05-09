package com.yoloo.android.feature.models.trendingblogs;

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
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.widget.timeview.TimeTextView;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;
import static com.airbnb.epoxy.EpoxyAttribute.Option.NoGetter;

@EpoxyModelClass(layout = R.layout.item_blog_trending)
public abstract class TrendingBlogModel
    extends EpoxyModelWithHolder<TrendingBlogModel.TrendingBlogHolder> {

  @EpoxyAttribute PostRealm post;
  @EpoxyAttribute boolean self;
  @EpoxyAttribute({ DoNotHash, NoGetter }) RequestManager glide;
  @EpoxyAttribute(DoNotHash) View.OnClickListener onClickListener;
  @EpoxyAttribute(DoNotHash) Transformation<Bitmap> bitmapTransformation;
  @EpoxyAttribute(DoNotHash) OnBookmarkClickListener onBookmarkClickListener;
  @EpoxyAttribute(DoNotHash) OnPostOptionsClickListener onPostOptionsClickListener;

  @Override
  public void bind(TrendingBlogHolder holder) {
    super.bind(holder);
    final Context context = holder.itemView.getContext();

    //noinspection unchecked
    glide
        .load(post.getAvatarUrl())
        .bitmapTransform(bitmapTransformation)
        .placeholder(R.drawable.ic_player_72dp)
        .into(holder.ivUserAvatar);

    holder.tvUsername.setText(post.getUsername());
    holder.tvTime.setTimeStamp(post.getCreated().getTime() / 1000);
    holder.tvBounty.setVisibility(post.getBounty() == 0 ? View.GONE : View.VISIBLE);
    holder.tvBounty.setText(String.valueOf(post.getBounty()));

    glide
        .load(post.getMedias().get(0).getThumbSizeUrl())
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
            holder.ivBlogCover.setImageDrawable(rbd);
          }
        });

    holder.tvBlogTitle.setText(post.getTitle());
    holder.tvBlogContent.setText(post.getContent());

    final int drawableIconRes =
        self ? R.drawable.ic_more_vert_black_24dp : R.drawable.ic_bookmark_black_24dp;
    holder.ibOptions.setImageDrawable(AppCompatResources.getDrawable(context, drawableIconRes));

    if (!self) {
      final int colorRes =
          post.isBookmarked() ? R.color.primary : android.R.color.secondary_text_dark;
      holder.ibOptions.setColorFilter(ContextCompat.getColor(context, colorRes),
          PorterDuff.Mode.SRC_IN);
    }

    holder.ibOptions.setOnClickListener(v -> {
      if (self) {
        onPostOptionsClickListener.onPostOptionsClick(v, post);
      } else {
        final int reversedColorRes =
            post.isBookmarked() ? android.R.color.secondary_text_dark : R.color.primary;
        holder.ibOptions.setColorFilter(ContextCompat.getColor(context, reversedColorRes),
            PorterDuff.Mode.SRC_IN);
        post.setBookmarked(!post.isBookmarked());
        onBookmarkClickListener.onBookmarkClick(post, post.isBookmarked());
      }
    });

    holder.itemView.setOnClickListener(v -> onClickListener.onClick(v));
  }

  @Override
  public void unbind(TrendingBlogHolder holder) {
    super.unbind(holder);
    holder.itemView.setOnClickListener(null);
    holder.ibOptions.setOnClickListener(null);
    Glide.clear(holder.ivBlogCover);
    holder.ivBlogCover.setImageDrawable(null);
  }

  static class TrendingBlogHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_feed_user_avatar) ImageView ivUserAvatar;
    @BindView(R.id.tv_item_feed_username) TextView tvUsername;
    @BindView(R.id.tv_item_feed_time) TimeTextView tvTime;
    @BindView(R.id.tv_item_feed_bounty) TextView tvBounty;
    @BindView(R.id.ib_item_feed_options) ImageButton ibOptions;
    @BindView(R.id.iv_item_blog_cover) ImageView ivBlogCover;
    @BindView(R.id.tv_item_blog_title) TextView tvBlogTitle;
    @BindView(R.id.tv_item_blog_content) TextView tvBlogContent;
  }
}
