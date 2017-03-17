package com.yoloo.android.data.feedtypes;

import com.yoloo.android.data.model.NewsRealm;
import java.util.List;

public class TravelNewsFeedItem implements FeedItem {
  private final List<NewsRealm> news;

  public TravelNewsFeedItem(List<NewsRealm> news) {
    this.news = news;
  }

  public List<NewsRealm> getNews() {
    return news;
  }
}
