package com.yoloo.android.feature.recentmedia;

import android.content.Context;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.recentmedias.RecentMediaModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import java.util.ArrayList;
import java.util.List;

import static com.yoloo.android.util.Preconditions.checkNotNull;

public class RecentMediaListEpoxyController
    extends Typed2EpoxyController<List<FeedItem<?>>, Boolean> {

  protected final RequestManager glide;

  protected List<FeedItem<?>> items;

  @AutoModel LoaderModel loader;

  private String userId;

  private OnItemClickListener<PostRealm> onItemClickListener;

  public RecentMediaListEpoxyController(Context context) {
    this.glide = Glide.with(context);
    this.items = new ArrayList<>(30);

    setData(items, false);
  }

  public void setOnItemClickListener(OnItemClickListener<PostRealm> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void showLoader() {
    setData(items, true);
  }

  public void hideLoader() {
    setData(items, false);
  }

  public void setLoadMoreData(List<FeedItem<?>> items) {
    this.items.addAll(items);
    setData(this.items, false);
  }

  @Override
  public void setData(List<FeedItem<?>> items, Boolean loadingMore) {
    this.items = items;
    super.setData(this.items, checkNotNull(loadingMore, "loadingMore cannot be null."));
  }

  @Override
  protected void buildModels(List<FeedItem<?>> feedItems, Boolean loadingMore) {
    Stream.of(feedItems).forEach(item -> createRecentMedia((PostRealm) item.getItem()));

    loader.addIf(loadingMore, this);
  }

  private void createRecentMedia(PostRealm post) {
    new RecentMediaModel_()
        .post(post)
        .id(post.getId())
        .glide(glide)
        .layout(R.layout.item_recent_media_big)
        .onClickListener(v -> onItemClickListener.onItemClick(v, post))
        .addTo(this);
  }
}
