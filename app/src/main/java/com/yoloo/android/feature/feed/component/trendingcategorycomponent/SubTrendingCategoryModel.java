package com.yoloo.android.feature.feed.component.trendingcategorycomponent;

import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;

@EpoxyModelClass(layout = R.layout.item_feed_sub_trending_category)
public abstract class SubTrendingCategoryModel
    extends EpoxyModelWithHolder<SubTrendingCategoryModel.CategoryHolder> {

  @EpoxyAttribute CategoryRealm category;
  @EpoxyAttribute(hash = false) OnItemClickListener<CategoryRealm> onItemClickListener;

  @Override public void bind(CategoryHolder holder) {
    Glide.with(holder.ivTopicBackground.getContext().getApplicationContext())
        .load(category.getBackgroundUrl())
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .into(holder.ivTopicBackground);

    holder.tvTopicText.setText(category.getName());

    holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(v, this, category));
  }

  static class CategoryHolder extends BaseEpoxyHolder {
    @BindView(R.id.iv_category_bg) ImageView ivTopicBackground;
    @BindView(R.id.tv_category_text) TextView tvTopicText;
  }
}
