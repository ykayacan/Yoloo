package com.yoloo.android.feature.feed.component.trendingcategorycomponent;

import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class FeedTrendingCategoryAdapter extends EpoxyAdapter {

  public void addTrendingCategories(List<CategoryRealm> items,
      OnItemClickListener<CategoryRealm> onItemClickListener) {
    addModels(createCategoryModel(items, onItemClickListener));
  }

  private List<SubTrendingCategoryModel_> createCategoryModel(List<CategoryRealm> items,
      OnItemClickListener<CategoryRealm> onItemClickListener) {
    return Stream.of(items)
        .map(category -> new SubTrendingCategoryModel_()
            .category(category)
            .onItemClickListener(onItemClickListener))
        .toList();
  }
}
