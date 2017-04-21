package com.yoloo.android.feature.feed.component.trendingcategory;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_trending_categories)
public abstract class FeedTrendingCategoryModel
    extends EpoxyModelWithHolder<FeedTrendingCategoryModel.FeedTrendingCategoriesHolder> {

  @EpoxyAttribute(hash = false) View.OnClickListener onHeaderClickListener;

  private FeedTrendingCategoryAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;
  private LinearLayoutManager lm;
  private SnapHelper snapHelper;

  public FeedTrendingCategoryModel(Context context, RequestManager glide) {
    adapter = new FeedTrendingCategoryAdapter(glide);
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

    if (onHeaderClickListener == null) {
      throw new IllegalStateException("onHeaderClickListener is null.");
    }

    holder.tvMore.setOnClickListener(v -> onHeaderClickListener.onClick(v));
  }

  public void addTrendingCategories(List<GroupRealm> items) {
    adapter.addTrendingCategories(items);
  }

  public void setOnItemClickListener(OnItemClickListener<GroupRealm> listener) {
    adapter.setOnItemClickListener(listener);
  }

  static class FeedTrendingCategoriesHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_feed_trending_category_more) TextView tvMore;
    @BindView(R.id.rv_feed_trending_category) RecyclerView rvTrendingCategory;
  }
}
