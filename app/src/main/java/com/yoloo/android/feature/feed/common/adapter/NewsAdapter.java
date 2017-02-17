package com.yoloo.android.feature.feed.common.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import com.airbnb.epoxy.EpoxyAdapter;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.feature.feed.common.model.NewsModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.List;

public class NewsAdapter extends EpoxyAdapter {

  public NewsAdapter(Context context) {
    /*glideRequest = Glide.with(context)
        .fromString()
        .asBitmap()
        .transcode(new PaletteBitmapTranscoder(context), PaletteBitmap.class)
        .diskCacheStrategy(DiskCacheStrategy.ALL);*/
  }

  public void addNews(List<NewsRealm> items, OnItemClickListener<NewsRealm> onItemClickListener) {
    for (NewsRealm news : items) {
      addModel(new NewsModel_()
          .news(news)
          .gradientDrawable(new GradientDrawable())
          .onItemClickListener(onItemClickListener));
    }
  }
}
