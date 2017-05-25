package com.yoloo.android.feature.postlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.feature.models.BountyButtonModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.post.BlogPostModel_;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.models.post.RichPostModel_;
import com.yoloo.android.feature.models.post.TextPostModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

import static com.yoloo.android.util.Preconditions.checkNotNull;

public class PostListEpoxyController extends Typed2EpoxyController<List<FeedItem<?>>, Boolean> {

  protected final RequestManager glide;
  protected final Transformation<Bitmap> bitmapTransformation;
  private final ConstraintSet constraintSet;

  protected List<FeedItem<?>> items;

  @AutoModel BountyButtonModel_ bountyButton;
  @AutoModel LoaderModel loader;

  private PostCallbacks postCallbacks;

  public PostListEpoxyController(Context context) {
    this.bitmapTransformation = new CropCircleTransformation(context);
    this.glide = Glide.with(context);
    this.constraintSet = new ConstraintSet();
    this.items = new ArrayList<>(20);
    setDebugLoggingEnabled(true);
  }

  public void setPostCallbacks(PostCallbacks postCallbacks) {
    this.postCallbacks = postCallbacks;
  }

  public void updatePost(@NonNull PostRealm post) {
    FeedItem<?> item = mapToPostFeedItem(post);
    if (item != null) {
      updateFeedItem(item);
    }
  }

  public void deletePost(PostRealm post) {
    items.remove(mapToPostFeedItem(post));
    setData(items, false);
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

  @Override public void setData(List<FeedItem<?>> items, Boolean loadingMore) {
    this.items = items;
    super.setData(this.items, checkNotNull(loadingMore, "loadingMore cannot be null."));
  }

  @Override protected void buildModels(List<FeedItem<?>> feedItems, Boolean loadingMore) {
    Stream.of(feedItems).forEach(item -> {
      if (item instanceof TextPostFeedItem) {
        createTextPost(((TextPostFeedItem) item).getItem());
      } else if (item instanceof RichPostFeedItem) {
        createRichPost(((RichPostFeedItem) item).getItem());
      } else if (item instanceof BlogPostFeedItem) {
        createBlogPost(((BlogPostFeedItem) item).getItem());
      }
    });

    //loader.addIf(loadingMore, this);
  }

  private void createTextPost(PostRealm post) {
    new TextPostModel_()
        .id(post.getId())
        .post(post)
        .groupName(post.getGroupId())
        .glide(glide)
        .transformation(bitmapTransformation)
        .callbacks(postCallbacks)
        .detailLayout(false)
        .addTo(this);
  }

  private void createRichPost(PostRealm post) {
    new RichPostModel_()
        .id(post.getId())
        .post(post)
        .groupName(post.getGroupId())
        .glide(glide)
        .transformation(bitmapTransformation)
        .callbacks(postCallbacks)
        .detailLayout(false)
        .set(constraintSet)
        .addTo(this);
  }

  private void createBlogPost(PostRealm post) {
    new BlogPostModel_()
        .id(post.getId())
        .post(post)
        .groupName(post.getGroupId())
        .glide(glide)
        .transformation(bitmapTransformation)
        .callbacks(postCallbacks)
        .detailLayout(false)
        .addTo(this);
  }

  protected void onMoreFeedItemType(FeedItem<?> item) {
  }

  private void updateFeedItem(@NonNull FeedItem<?> item) {
    final int size = items.size();
    for (int i = 0; i < size; i++) {
      if (items.get(i).id().equals(item.id())) {
        items.set(i, item);
        break;
      }
    }

    setData(items, false);
  }

  @Nullable private FeedItem<?> mapToPostFeedItem(PostRealm post) {
    if (post.isTextPost()) {
      return new TextPostFeedItem(post);
    } else if (post.isRichPost()) {
      return new RichPostFeedItem(post);
    } else if (post.isBlogPost()) {
      return new BlogPostFeedItem(post);
    }

    return null;
  }
}
