package com.yoloo.android.feature.models.trendingblogs;

import android.support.annotation.NonNull;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelGroup;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.TrendingBlogListFeedItem;
import java.util.ArrayList;
import java.util.List;

public class TrendingBlogListModelGroup extends EpoxyModelGroup {

  public TrendingBlogListModelGroup(TrendingBlogListFeedItem item, Callbacks callbacks,
      RequestManager glide) {
    super(R.layout.item_trending_blogs, buildModels(item, callbacks, glide));
    id(item.getId());
  }

  private static List<EpoxyModel<?>> buildModels(TrendingBlogListFeedItem item, Callbacks callbacks,
      RequestManager glide) {
    List<EpoxyModel<?>> models = new ArrayList<>();

    models.add(new SimpleEpoxyModel(R.layout.item_trending_blog_header_text));

    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_more_text)
        .onClick(v -> callbacks.onTrendingBlogHeaderClicked()));

    // inner group models
    List<TrendingBlogModel_> blogModels = Stream
        .of(item.getItem())
        .map(post -> new TrendingBlogModel_()
            .id(post.getId())
            .post(post)
            .glide(glide)
            .onBookmarkClickListener(v -> callbacks.onTrendingBlogBookmarkClicked(post))
            .onClickListener(v -> callbacks.onTrendingBlogClicked(post)))
        .toList();

    // inner recyclerview
    models.add(new TrendingBlogListModel_().numItemsExpectedOnDisplay(5).models(blogModels));

    return models;
  }

  public interface Callbacks {
    void onTrendingBlogHeaderClicked();

    void onTrendingBlogClicked(@NonNull PostRealm blog);

    void onTrendingBlogBookmarkClicked(@NonNull PostRealm post);
  }
}
