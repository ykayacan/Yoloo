package com.yoloo.android.feature.feed.component.trendingcategory;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

class FeedTrendingCategoryAdapter extends EpoxyAdapter {

  private OnItemClickListener<GroupRealm> onItemClickListener;
  private final RequestManager glide;

  FeedTrendingCategoryAdapter(RequestManager glide) {
    this.glide = glide;
  }

  void addTrendingCategories(List<GroupRealm> items) {
    addModels(createModels(items));
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> listener) {
    this.onItemClickListener = listener;
  }

  private List<FeedTrendingCategoryAdapter$SubTrendingCategoryModel_> createModels(
      List<GroupRealm> items) {
    if (onItemClickListener == null) {
      throw new IllegalStateException("onItemClickListener is null.");
    }

    return Stream.of(items)
        .map(category -> new FeedTrendingCategoryAdapter$SubTrendingCategoryModel_().glide(glide)
            .category(category)
            .onItemClickListener(onItemClickListener))
        .toList();
  }

  @EpoxyModelClass(layout = R.layout.item_feed_sub_trending_category)
  static abstract class SubTrendingCategoryModel
      extends EpoxyModelWithHolder<SubTrendingCategoryModel.CategoryHolder> {

    @EpoxyAttribute GroupRealm category;
    @EpoxyAttribute(hash = false) RequestManager glide;
    @EpoxyAttribute(hash = false) OnItemClickListener<GroupRealm> onItemClickListener;

    @Override public void bind(SubTrendingCategoryModel.CategoryHolder holder) {
      final Context context = holder.itemView.getContext();

      glide.load(category.getBackgroundUrl() + "=s150-rw")
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
}
