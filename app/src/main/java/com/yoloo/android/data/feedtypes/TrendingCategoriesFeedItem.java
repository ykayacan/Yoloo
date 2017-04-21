package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.GroupRealm;
import java.util.List;

public class TrendingCategoriesFeedItem implements FeedItem {
  private final List<GroupRealm> categories;

  public TrendingCategoriesFeedItem(List<GroupRealm> categories) {
    this.categories = categories;
  }

  public List<GroupRealm> getCategories() {
    return categories;
  }
}
