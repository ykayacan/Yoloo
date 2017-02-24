package com.yoloo.android.feature.news;

import android.graphics.drawable.GradientDrawable;
import com.airbnb.epoxy.EpoxyAdapter;
import com.annimon.stream.Stream;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class NewsAdapter extends EpoxyAdapter {

  private final GradientDrawable gradientDrawable;
  private final OnItemClickListener<NewsRealm> onItemClickListener;

  public NewsAdapter(OnItemClickListener<NewsRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
    gradientDrawable = new GradientDrawable();
  }

  public void addNews(List<NewsRealm> items) {
    addModels(createNewsModel(items));
  }

  private List<NewsModel_> createNewsModel(List<NewsRealm> items) {
    return Stream.of(items)
        .map(news -> new NewsModel_()
            .news(news)
            .gradientDrawable(gradientDrawable)
            .onItemClickListener(onItemClickListener))
        .toList();
  }
}
