package com.yoloo.android.feature.feed.common.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.graphics.Palette;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.ScrimUtil;
import com.yoloo.android.util.TextViewUtil;

@EpoxyModelClass(layout = R.layout.item_news)
public abstract class NewsModel extends EpoxyModelWithHolder<NewsModel.NewsHolder> {

  @EpoxyAttribute NewsRealm news;
  @EpoxyAttribute(hash = false) OnItemClickListener<NewsRealm> onItemClickListener;
  @EpoxyAttribute(hash = false) GradientDrawable gradientDrawable;

  @Override public void bind(NewsHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide.with(context)
        .load(news.getBgImageUrl())
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(new SimpleTarget<Bitmap>() {
          @SuppressLint("NewApi") @Override public void onResourceReady(Bitmap resource,
              GlideAnimation<? super Bitmap> glideAnimation) {
            Palette.from(resource).generate(palette -> {
              final int vibrantColor = palette.getVibrantColor(Color.BLACK);

              holder.ivNewsBg.setImageBitmap(resource);
              holder.ivNewsBg.setForeground(
                  ScrimUtil.makeCubicGradientScrimDrawable(vibrantColor, 3, Gravity.BOTTOM));
            });
          }
        });

    final int screenWidth = DisplayUtil.getScreenWidth();

    holder.itemView.getViewTreeObserver().addOnPreDrawListener(
        new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            if (holder.itemView.getMeasuredWidth() > screenWidth / 2) {
              TextViewUtil.setTextAppearance(holder.tvNewsTitle, context,
                  R.style.TextAppearance_FeedNewsBigTitle);
            } else {
              TextViewUtil.setTextAppearance(holder.tvNewsTitle, context,
                  R.style.TextAppearance_FeedNewsSmallTitle);
            }
            holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
          }
        });

    holder.tvNewsTitle.setText(news.getTitle());

    holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, news));
  }

  @Override public void unbind(NewsHolder holder) {
    Glide.clear(holder.ivNewsBg);
    holder.ivNewsBg.setImageDrawable(null);
  }

  @Override public int getSpanSize(int totalSpanCount, int position, int itemCount) {
    return position % 3 == 0 ? 2 : 1;
  }

  static class NewsHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_item_news_bg) ImageView ivNewsBg;
    @BindView(R.id.tv_item_news_title) TextView tvNewsTitle;
  }
}
