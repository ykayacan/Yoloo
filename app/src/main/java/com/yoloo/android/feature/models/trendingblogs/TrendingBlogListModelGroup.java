package com.yoloo.android.feature.models.trendingblogs;

import android.graphics.Bitmap;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelGroup;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.TrendingBlogListItem;
import com.yoloo.android.feature.feed.FeedEpoxyController;
import java.util.ArrayList;
import java.util.List;

public class TrendingBlogListModelGroup extends EpoxyModelGroup {

  public TrendingBlogListModelGroup(String userId, TrendingBlogListItem item,
      FeedEpoxyController.TrendingBlogListCallbacks callbacks, RequestManager glide,
      Transformation<Bitmap> bitmapTransformation) {
    super(R.layout.item_trending_blogs,
        buildModels(userId, item, callbacks, glide, bitmapTransformation));
    id(item.getId());
  }

  private static List<EpoxyModel<?>> buildModels(String userId, TrendingBlogListItem item,
      FeedEpoxyController.TrendingBlogListCallbacks callbacks, RequestManager glide,
      Transformation<Bitmap> bitmapTransformation) {
    List<EpoxyModel<?>> models = new ArrayList<>();

    models.add(new SimpleEpoxyModel(R.layout.item_trending_blog_header_text));
    models.add(new SimpleEpoxyModel(R.layout.item_recommended_group_more_text).onClick(
        v -> callbacks.onTrendingBlogHeaderClicked()));

    // inner group models
    List<TrendingBlogModel_> blogModels = Stream
        .of(item.getTrendingBlogs())
        .map(post -> new TrendingBlogModel_()
            .id(post.getId())
            .post(post)
            .glide(glide)
            .self(post.getOwnerId().equals(userId))
            .bitmapTransformation(bitmapTransformation)
            .onPostOptionsClickListener(
                (v, post1) -> callbacks.onTrendingBlogOptionsClicked(v, post))
            .onBookmarkClickListener(
                (postRealm, bookmark) -> callbacks.onTrendingBlogBookmarkClicked(postRealm.getId(),
                    bookmark))
            .onClickListener(v -> callbacks.onTrendingBlogClicked(post)))
        .toList();

    // inner recyclerview
    models.add(new TrendingBlogListModel_().numItemsExpectedOnDisplay(5).models(blogModels));

    return models;
  }
}
