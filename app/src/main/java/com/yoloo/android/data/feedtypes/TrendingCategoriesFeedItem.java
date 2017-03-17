package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.CategoryRealm;
import java.util.List;

public class TrendingCategoriesFeedItem implements FeedItem {
  private final List<CategoryRealm> categories;

  public TrendingCategoriesFeedItem(List<CategoryRealm> categories) {
    this.categories = categories;
  }

  public List<CategoryRealm> getCategories() {
    return categories;
  }
}
