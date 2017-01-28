package com.yoloo.android.feature.feed.common.model;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.adapter.TrendingCategoryAdapter;
import com.yoloo.android.feature.ui.recyclerview.BaseEpoxyHolder;
import java.util.List;

public class TrendingCategoryModel
    extends EpoxyModelWithHolder<TrendingCategoryModel.TrendingCategoriesHolder> {

  @EpoxyAttribute(hash = false) RecyclerView.ItemAnimator itemAnimator;
  @EpoxyAttribute(hash = false) TrendingCategoryAdapter adapter;
  @EpoxyAttribute(hash = false) RecyclerView.LayoutManager layoutManager;
  @EpoxyAttribute(hash = false) SnapHelper snapHelper;
  @EpoxyAttribute(hash = false) FeedAdapter.OnCategoryClickListener onCategoryClickListener;
  @EpoxyAttribute(hash = false) FeedAdapter.OnExploreCategoriesClickListener
      onExploreCategoriesClickListener;

  @Override protected TrendingCategoriesHolder createNewHolder() {
    return new TrendingCategoriesHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_feed_trending_categories;
  }

  @Override public void bind(TrendingCategoriesHolder holder) {
    holder.tvTrendingNow.setText(R.string.label_feed_trending_now);
    holder.tvCategoryExplore.setText(R.string.label_feed_explore);

    holder.rvTrendingCategory.setAdapter(adapter);
    holder.rvTrendingCategory.setLayoutManager(layoutManager);
    holder.rvTrendingCategory.setHasFixedSize(true);
    holder.rvTrendingCategory.setItemAnimator(itemAnimator);
    holder.rvTrendingCategory.setOnFlingListener(null);
    snapHelper.attachToRecyclerView(holder.rvTrendingCategory);
    adapter.setOnCategoryClickListener(onCategoryClickListener);

    holder.tvCategoryExplore.setOnClickListener(
        v -> onExploreCategoriesClickListener.onExploreCategoriesClick(v));
  }

  @Override public void unbind(TrendingCategoriesHolder holder) {
    holder.tvCategoryExplore.setOnClickListener(null);
  }

  public void updateTrendingCategories(List<CategoryRealm> items) {
    adapter.updateTrendingCategories(items);
  }

  static class TrendingCategoriesHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_category_trending_now) TextView tvTrendingNow;
    @BindView(R.id.tv_category_explore) TextView tvCategoryExplore;
    @BindView(R.id.rv_category_trending) RecyclerView rvTrendingCategory;
  }
}