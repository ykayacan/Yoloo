package com.yoloo.android.feature.feed.common.adapter;

import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.model.CategoryModel_;
import java.util.List;

public class TrendingCategoryAdapter extends EpoxyAdapter {

  public TrendingCategoryAdapter() {
    enableDiffing();
  }

  public void addTrendingCategories(List<CategoryRealm> items,
      FeedAdapter.OnCategoryClickListener onCategoryClickListener) {
    for (CategoryRealm category : items) {
      models.add(new CategoryModel_()
          .category(category)
          .onCategoryClickListener(onCategoryClickListener));
    }

    notifyModelsChanged();
  }
}