package com.yoloo.android.feature.feed.component.trendingblogs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.ui.recyclerview.BaseEpoxyHolder;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import java.util.List;

@EpoxyModelClass(layout = R.layout.item_feed_trending_blogs)
public abstract class FeedTrendingBlogsModel
    extends EpoxyModelWithHolder<FeedTrendingBlogsModel.FeedTrendingBlogsHolder> {

  @EpoxyAttribute(hash = false) View.OnClickListener onHeaderClickListener;

  private FeedTrendingBlogsAdapter adapter;
  private RecyclerView.ItemDecoration itemDecoration;
  private LinearLayoutManager lm;
  private SnapHelper snapHelper;

  public FeedTrendingBlogsModel(Context context, RequestManager glide) {
    adapter = new FeedTrendingBlogsAdapter(glide);
    itemDecoration = new SpaceItemDecoration(16, SpaceItemDecoration.HORIZONTAL);
    snapHelper = new LinearSnapHelper();
    lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    lm.setInitialPrefetchItemCount(2);
  }

  @Override public void bind(FeedTrendingBlogsHolder holder) {
    holder.rvTrendingBlogs.removeItemDecoration(itemDecoration);
    holder.rvTrendingBlogs.addItemDecoration(itemDecoration);

    holder.rvTrendingBlogs.setLayoutManager(lm);
    holder.rvTrendingBlogs.setHasFixedSize(true);

    holder.rvTrendingBlogs.setOnFlingListener(null);
    snapHelper.attachToRecyclerView(holder.rvTrendingBlogs);

    if (holder.rvTrendingBlogs.getAdapter() == null) {
      holder.rvTrendingBlogs.setAdapter(adapter);
    }

    if (onHeaderClickListener == null) {
      throw new IllegalStateException("onHeaderClickListener is null.");
    }

    holder.tvMore.setOnClickListener(v -> onHeaderClickListener.onClick(v));
  }

  public void addTrendingBlogs(List<PostRealm> items) {
    adapter.addTrendingBlogs(items);
  }

  public void setOnItemClickListener(OnItemClickListener<PostRealm> listener) {
    adapter.setOnItemClickListener(listener);
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener listener) {
    adapter.setOnPostOptionsClickListener(listener);
  }

  public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
    adapter.setOnBookmarkClickListener(listener);
  }

  public void setUserId(String userId) {
    adapter.setUserId(userId);
  }

  static class FeedTrendingBlogsHolder extends BaseEpoxyHolder {
    @BindView(R.id.tv_feed_trending_blogs_more) TextView tvMore;
    @BindView(R.id.rv_feed_trending_blogs) RecyclerView rvTrendingBlogs;
  }
}
