package com.yoloo.android.feature.feed.component.trendingcategory;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

@EpoxyModelClass(layout = R.layout.item_feed_sub_trending_category)
abstract class SubTrendingCategoryModel
    extends EpoxyModelWithHolder<SubTrendingCategoryModel.CategoryHolder> {

  @EpoxyAttribute CategoryRealm category;
  @EpoxyAttribute(hash = false) OnItemClickListener<CategoryRealm> onItemClickListener;

  @Override public void bind(CategoryHolder holder) {
    final Context context = holder.itemView.getContext();

    Glide.with(context)
        .load(category.getBackgroundUrl() + "=s80-rw")
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .into(new SimpleTarget<Bitmap>() {
          @Override
          public void onResourceReady(Bitmap resource,
              GlideAnimation<? super Bitmap> glideAnimation) {
            RoundedBitmapDrawable rbd =
                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
            rbd.setCornerRadius(6f);
            holder.ivTopicBackground.setImageDrawable(rbd);
          }
        });

    holder.tvTopicText.setText(category.getName());

    holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, category));
  }

  static class CategoryHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_category_bg) ImageView ivTopicBackground;
    @BindView(R.id.tv_category_text) TextView tvTopicText;
  }
}
