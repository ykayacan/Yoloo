package com.yoloo.android.feature.feed.common.adapter;

import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.feed.common.model.CategoryModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class FeedTrendingCategoryAdapter extends EpoxyAdapter {

  public FeedTrendingCategoryAdapter() {
    enableDiffing();
  }

  public void addTrendingCategories(List<CategoryRealm> items,
      OnItemClickListener<CategoryRealm> onItemClickListener) {
    models.addAll(Stream.of(items).map(category -> new CategoryModel_()
        .category(category)
        .onItemClickListener(onItemClickListener)).collect(Collectors.toList()));

    notifyModelsChanged();
  }
}