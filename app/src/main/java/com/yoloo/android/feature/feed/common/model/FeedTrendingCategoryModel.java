package com.yoloo.android.feature.feed.common.model;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.adapter.FeedTrendingCategoryAdapter;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_trending_categories)
public abstract class FeedTrendingCategoryModel
    extends EpoxyModelWithHolder<FeedTrendingCategoryModel.FeedTrendingCategoriesHolder> {

  @EpoxyAttribute(hash = false) View.OnClickListener headerClickListener;

  private FeedTrendingCategoryAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;
  private LinearLayoutManager lm;
  private SnapHelper snapHelper;

  public FeedTrendingCategoryModel(Context context) {
    adapter = new FeedTrendingCategoryAdapter();
    itemDecoration = new SpaceItemDecoration(4, SpaceItemDecoration.HORIZONTAL);
    snapHelper = new LinearSnapHelper();

    lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    lm.setInitialPrefetchItemCount(4);
  }

  @Override public void bind(FeedTrendingCategoriesHolder holder) {
    holder.rvTrendingCategory.removeItemDecoration(itemDecoration);
    holder.rvTrendingCategory.addItemDecoration(itemDecoration);

    holder.rvTrendingCategory.setLayoutManager(lm);
    holder.rvTrendingCategory.setHasFixedSize(true);

    holder.rvTrendingCategory.setOnFlingListener(null);
    snapHelper.attachToRecyclerView(holder.rvTrendingCategory);

    if (holder.rvTrendingCategory.getAdapter() == null) {
      holder.rvTrendingCategory.setAdapter(adapter);
    }

    holder.layoutHeader.setOnClickListener(v -> headerClickListener.onClick(v));
  }

  @Override public void unbind(FeedTrendingCategoriesHolder holder) {
    holder.layoutHeader.setOnClickListener(null);
  }

  public void addTrendingCategories(List<CategoryRealm> items,
      OnItemClickListener<CategoryRealm> onItemClickListener) {
    adapter.addTrendingCategories(items, onItemClickListener);
  }

  static class FeedTrendingCategoriesHolder extends BaseEpoxyHolder {
    @BindView(R.id.layout_feed_category_trending_header) ViewGroup layoutHeader;
    @BindView(R.id.rv_feed_category_trending) RecyclerView rvTrendingCategory;
  }
}
