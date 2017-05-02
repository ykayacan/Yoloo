package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.PostRealm;
import java.util.List;

public class TrendingBlogListItem implements FeedItem {
  private final String id;
  private final List<PostRealm> trendingBlogs;

  public TrendingBlogListItem(List<PostRealm> trendingBlogs) {
    this.trendingBlogs = trendingBlogs;
    this.id = TrendingBlogListItem.class.getName();
  }

  public String getId() {
    return id;
  }

  public List<PostRealm> getTrendingBlogs() {
    return trendingBlogs;
  }
}
