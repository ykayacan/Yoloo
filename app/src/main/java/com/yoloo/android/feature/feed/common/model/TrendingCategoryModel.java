package com.yoloo.android.feature.feed.common.model;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
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

  @EpoxyAttribute(hash = false) FeedAdapter.OnCategoryClickListener onCategoryClickListener;
  @EpoxyAttribute(hash = false) FeedAdapter.OnExploreCategoriesClickListener
      onExploreCategoriesClickListener;

  private TrendingCategoryAdapter adapter = new TrendingCategoryAdapter();

  @Override protected TrendingCategoriesHolder createNewHolder() {
    return new TrendingCategoriesHolder();
  }

  @Override protected int getDefaultLayout() {
    return R.layout.item_feed_trending_categories;
  }

  @Override public void bind(TrendingCategoriesHolder holder) {
    holder.tvTrendingNow.setText(R.string.label_feed_trending_now);
    holder.tvCategoryExplore.setText(R.string.label_feed_explore);

    final Context context = holder.rvTrendingCategory.getContext();

    holder.rvTrendingCategory.setAdapter(adapter);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false);
    layoutManager.setInitialPrefetchItemCount(4);
    holder.rvTrendingCategory.setLayoutManager(layoutManager);
    holder.rvTrendingCategory.setHasFixedSize(true);
    holder.rvTrendingCategory.setOnFlingListener(null);
    final SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(holder.rvTrendingCategory);

    holder.tvCategoryExplore.setOnClickListener(
        v -> onExploreCategoriesClickListener.onExploreCategoriesClick(v));
  }

  @Override public void unbind(TrendingCategoriesHolder holder) {
    holder.tvCategoryExplore.setOnClickListener(null);
  }

  public void addTrendingCategories(List<CategoryRealm> items,
      FeedAdapter.OnCategoryClickListener onCategoryClickListener) {
    adapter.addTrendingCategories(items, onCategoryClickListener);
  }

  static class TrendingCategoriesHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_category_trending_now) TextView tvTrendingNow;
    @BindView(R.id.tv_category_explore) TextView tvCategoryExplore;
    @BindView(R.id.rv_category_trending) RecyclerView rvTrendingCategory;
  }
}