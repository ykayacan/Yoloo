package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.PostRealm;
import java.util.List;

public class TrendingBlogsFeedItem implements FeedItem {
  private final List<PostRealm> trendingBlogs;

  public TrendingBlogsFeedItem(List<PostRealm> trendingBlogs) {
    this.trendingBlogs = trendingBlogs;
  }

  public List<PostRealm> getTrendingBlogs() {
    return trendingBlogs;
  }
}
