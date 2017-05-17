package com.yoloo.android.feature.postlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.view.View;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.BountyButtonFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.NewUserListFeedItem;
import com.yoloo.android.data.feed.RecommendedGroupListFeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.data.feed.TrendingBlogListFeedItem;
import com.yoloo.android.feature.models.BountyButtonModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.newusers.NewUserListModelGroup;
import com.yoloo.android.feature.models.post.BlogPostModel_;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.models.post.RichPostModel_;
import com.yoloo.android.feature.models.post.TextPostModel_;
import com.yoloo.android.feature.models.recommendedgroups.RecommendedGroupListModelGroup;
import com.yoloo.android.feature.models.trendingblogs.TrendingBlogListModelGroup;
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

  private String userId;

  private PostCallbacks postCallbacks;
  private RecommendedGroupListModelGroup.Callbacks recommendedGroupListCallbacks;
  private TrendingBlogListModelGroup.Callbacks trendingBlogListCallbacks;
  private NewUserListModelGroup.Callbacks newUserListModelGroupCallbacks;
  private View.OnClickListener onBountyButtonClickListener;

  public PostListEpoxyController(Context context) {
    this.bitmapTransformation = new CropCircleTransformation(context);
    this.glide = Glide.with(context);
    this.constraintSet = new ConstraintSet();
    this.items = new ArrayList<>(20);
    setDebugLoggingEnabled(true);
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setPostCallbacks(PostCallbacks postCallbacks) {
    this.postCallbacks = postCallbacks;
  }

  public void setRecommendedGroupListCallbacks(
      RecommendedGroupListModelGroup.Callbacks recommendedGroupListCallbacks) {
    this.recommendedGroupListCallbacks = recommendedGroupListCallbacks;
  }

  public void setTrendingBlogListCallbacks(
      TrendingBlogListModelGroup.Callbacks trendingBlogListCallbacks) {
    this.trendingBlogListCallbacks = trendingBlogListCallbacks;
  }

  public void setNewUserListModelGroupCallbacks(
      NewUserListModelGroup.Callbacks newUserListModelGroupCallbacks) {
    this.newUserListModelGroupCallbacks = newUserListModelGroupCallbacks;
  }

  public void setOnBountyButtonClickListener(View.OnClickListener onBountyButtonClickListener) {
    this.onBountyButtonClickListener = onBountyButtonClickListener;
  }

  public void updatePost(@NonNull PostRealm post) {
    if (post.isTextPost()) {
      updateFeedItem(new TextPostFeedItem(post));
    } else if (post.isRichPost()) {
      updateFeedItem(new RichPostFeedItem(post));
    } else if (post.isBlogPost()) {
      updateFeedItem(new BlogPostFeedItem(post));
    }
  }

  public void addPost(PostRealm post, int position) {
    if (post.isTextPost()) {
      items.add(position, new TextPostFeedItem(post));
    } else if (post.isRichPost()) {
      items.add(position, new RichPostFeedItem(post));
    } else if (post.isBlogPost()) {
      items.add(position, new BlogPostFeedItem(post));
    }

    setData(items, false);
  }

  public void deletePost(PostRealm post) {
    if (post.isTextPost()) {
      items.remove(new TextPostFeedItem(post));
    } else if (post.isRichPost()) {
      items.remove(new RichPostFeedItem(post));
    } else if (post.isBlogPost()) {
      items.remove(new BlogPostFeedItem(post));
    }

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
      if (item instanceof RecommendedGroupListFeedItem) {
        new RecommendedGroupListModelGroup((RecommendedGroupListFeedItem) item,
            recommendedGroupListCallbacks, glide).addTo(this);
      } else if (item instanceof TrendingBlogListFeedItem) {
        new TrendingBlogListModelGroup(userId, ((TrendingBlogListFeedItem) item),
            trendingBlogListCallbacks, glide, bitmapTransformation).addTo(this);
      } else if (item instanceof BountyButtonFeedItem) {
        bountyButton.onClickListener(onBountyButtonClickListener).addTo(this);
      } else if (item instanceof TextPostFeedItem) {
        createTextPost(((TextPostFeedItem) item).getItem());
      } else if (item instanceof RichPostFeedItem) {
        createRichPost(((RichPostFeedItem) item).getItem());
      } else if (item instanceof BlogPostFeedItem) {
        createBlogPost(((BlogPostFeedItem) item).getItem());
      } else if (item instanceof NewUserListFeedItem) {
        new NewUserListModelGroup((NewUserListFeedItem) item, newUserListModelGroupCallbacks,
            glide).addIf(!((NewUserListFeedItem) item).getItem().isEmpty(), this);
      } else {
        onMoreFeedItemType(item);
      }
    });

    //loader.addIf(loadingMore, this);
  }

  private void createTextPost(PostRealm post) {
    new TextPostModel_()
        .id(post.getId())
        .post(post)
        .postOwner(post.getOwnerId().equals(userId))
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
        .postOwner(post.getOwnerId().equals(userId))
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
        .postOwner(post.getOwnerId().equals(userId))
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
}
