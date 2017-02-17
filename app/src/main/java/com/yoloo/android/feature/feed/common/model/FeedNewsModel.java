package com.yoloo.android.feature.feed.common.model;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.yoloo.android.R;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.feature.feed.common.adapter.NewsAdapter;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.ItemDecorationAlbumColumns;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_news)
public abstract class FeedNewsModel extends EpoxyModelWithHolder<FeedNewsModel.FeedNewsHolder> {

  private static final int SPAN_COUNT = 3;

  @EpoxyAttribute(hash = false) View.OnClickListener headerClickListener;

  private NewsAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;
  private GridLayoutManager lm;

  public FeedNewsModel(Context context) {
    adapter = new NewsAdapter(context);
    adapter.setSpanCount(SPAN_COUNT);
    itemDecoration = new ItemDecorationAlbumColumns(1, 2);

    lm = new GridLayoutManager(context, SPAN_COUNT);
    lm.setSpanSizeLookup(adapter.getSpanSizeLookup());
  }

  @Override public void bind(FeedNewsHolder holder) {
    holder.rvNews.removeItemDecoration(itemDecoration);
    holder.rvNews.addItemDecoration(itemDecoration);

    holder.rvNews.setLayoutManager(lm);
    holder.rvNews.setHasFixedSize(true);

    if (holder.rvNews.getAdapter() == null) {
      holder.rvNews.setAdapter(adapter);
    }

    holder.layoutHeader.setOnClickListener(v -> headerClickListener.onClick(v));
  }

  public void addNews(List<NewsRealm> items, OnItemClickListener<NewsRealm> onItemClickListener) {
    adapter.addNews(items, onItemClickListener);
  }

  static class FeedNewsHolder extends BaseEpoxyHolder {
    @BindView(R.id.layout_feed_travel_news_header) ViewGroup layoutHeader;
    @BindView(R.id.rv_feed_news) RecyclerView rvNews;
  }
}
