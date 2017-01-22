package com.yoloo.android.feature.feed.common.adapter;

import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.model.CategoryModel_;
import java.util.List;

public class TrendingCategoryAdapter extends EpoxyAdapter {

  private FeedAdapter.OnCategoryClickListener onCategoryClickListener;

  public TrendingCategoryAdapter() {
    enableDiffing();
  }

  public void setOnCategoryClickListener(FeedAdapter.OnCategoryClickListener listener) {
    this.onCategoryClickListener = listener;
  }

  public void updateTrendingCategories(List<CategoryRealm> items) {
    models.clear();

    for (CategoryRealm realm : items) {
      CategoryModel_ model_ =
          new CategoryModel_().realm(realm).onCategoryClickListener(onCategoryClickListener);

      models.add(model_);
    }

    notifyModelsChanged();
  }
}