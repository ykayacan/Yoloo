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
import com.yoloo.android.feature.base.BaseEpoxyHolder;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.adapter.FeedAdapter;
import com.yoloo.android.feature.feed.common.adapter.TrendingCategoryAdapter;
import com.yoloo.android.feature.ui.recyclerview.SlideInItemAnimator;
import com.yoloo.android.feature.ui.recyclerview.SpaceItemDecoration;
import java.util.List;

public class TrendingCategoryModel
    extends EpoxyModelWithHolder<TrendingCategoryModel.TrendingCategoriesHolder> {

  private final SnapHelper snapHelper = new LinearSnapHelper();
  private final RecyclerView.ItemDecoration decoration =
      new SpaceItemDecoration(4, SpaceItemDecoration.HORIZONTAL);

  @EpoxyAttribute(hash = false)
  FeedAdapter.OnCategoryClickListener onCategoryClickListener;

  @EpoxyAttribute(hash = false)
  FeedAdapter.OnExploreCategoriesClickListener onExploreCategoriesClickListener;

  private TrendingCategoryAdapter adapter;
  private LinearLayoutManager layoutManager;

  @Override
  protected TrendingCategoriesHolder createNewHolder() {
    return new TrendingCategoriesHolder();
  }

  @Override
  protected int getDefaultLayout() {
    return R.layout.item_feed_trending_categories;
  }

  @Override
  public void bind(TrendingCategoriesHolder holder) {
    holder.tvTrendingNow.setText(R.string.label_feed_trending_now);
    holder.tvCategoryExplore.setText(R.string.label_feed_explore);

    holder.rvTrendingCategory.setAdapter(getAdapter());
    LinearLayoutManager layoutManager = getLayoutManager(holder.rvTrendingCategory.getContext());
    layoutManager.setItemPrefetchEnabled(true);
    layoutManager.setInitialPrefetchItemCount(7);

    holder.rvTrendingCategory.setLayoutManager(layoutManager);
    holder.rvTrendingCategory.addItemDecoration(decoration);
    holder.rvTrendingCategory.setHasFixedSize(true);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    holder.rvTrendingCategory.setItemAnimator(animator);

    holder.rvTrendingCategory.setOnFlingListener(null);
    snapHelper.attachToRecyclerView(holder.rvTrendingCategory);
    getAdapter().setOnCategoryClickListener(onCategoryClickListener);

    holder.tvCategoryExplore.setOnClickListener(
        v -> onExploreCategoriesClickListener.onExploreCategoriesClick(v));
  }

  @Override
  public void unbind(TrendingCategoriesHolder holder) {
    holder.tvCategoryExplore.setOnClickListener(null);
  }

  public void updateTrendingCategories(List<CategoryRealm> items) {
    adapter.updateTrendingCategories(items);
  }

  private TrendingCategoryAdapter getAdapter() {
    if (adapter == null) {
      adapter = new TrendingCategoryAdapter();
    }
    return adapter;
  }

  private LinearLayoutManager getLayoutManager(Context context) {
    if (layoutManager == null) {
      layoutManager = new LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false);
    }
    return layoutManager;
  }

  static class TrendingCategoriesHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_category_trending_now)
    TextView tvTrendingNow;

    @BindView(R.id.tv_category_explore)
    TextView tvCategoryExplore;

    @BindView(R.id.rv_category_trending)
    RecyclerView rvTrendingCategory;
  }
}