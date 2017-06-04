package com.yoloo.android.feature.feed;

import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.view.View;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.BountyButtonFeedItem;
import com.yoloo.android.data.feed.NewUserListFeedItem;
import com.yoloo.android.data.feed.NewUserWelcomeFeedItem;
import com.yoloo.android.data.feed.RecommendedGroupListFeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.data.feed.TrendingBlogListFeedItem;
import com.yoloo.android.feature.models.BountyButtonModel;
import com.yoloo.android.feature.models.BountyButtonModel_;
import com.yoloo.android.feature.models.NewUserWelcomeModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.newusers.NewUserListModelGroup;
import com.yoloo.android.feature.models.post.BlogPostModel_;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.models.post.RichPostModel_;
import com.yoloo.android.feature.models.post.TextPostModel_;
import com.yoloo.android.feature.models.recommendedgroups.RecommendedGroupListModelGroup;
import com.yoloo.android.feature.models.trendingblogs.TrendingBlogListModelGroup;

class FeedEpoxyController extends TypedEpoxyController<FeedPresenter.FeedState> {

  private final RequestManager glide;
  private final ConstraintSet constraintSet;

  @AutoModel BountyButtonModel_ bountyButton;
  @AutoModel LoaderModel loader;

  private PostCallbacks postCallbacks;
  private RecommendedGroupListModelGroup.Callbacks recommendedGroupListCallbacks;
  private TrendingBlogListModelGroup.Callbacks trendingBlogListCallbacks;
  private NewUserListModelGroup.Callbacks newUserListModelGroupCallbacks;
  private BountyButtonModel.OnBountyClickListener onBountyButtonClickListener;
  private View.OnClickListener onNewUserWelcomeClickListener;

  FeedEpoxyController(Context context) {
    this.glide = Glide.with(context);
    this.constraintSet = new ConstraintSet();
    setDebugLoggingEnabled(false);
  }

  void setPostCallbacks(PostCallbacks postCallbacks) {
    this.postCallbacks = postCallbacks;
  }

  void setRecommendedGroupListCallbacks(
      RecommendedGroupListModelGroup.Callbacks recommendedGroupListCallbacks) {
    this.recommendedGroupListCallbacks = recommendedGroupListCallbacks;
  }

  void setTrendingBlogListCallbacks(
      TrendingBlogListModelGroup.Callbacks trendingBlogListCallbacks) {
    this.trendingBlogListCallbacks = trendingBlogListCallbacks;
  }

  void setNewUserListModelGroupCallbacks(
      NewUserListModelGroup.Callbacks newUserListModelGroupCallbacks) {
    this.newUserListModelGroupCallbacks = newUserListModelGroupCallbacks;
  }

  void setOnBountyButtonClickListener(
      BountyButtonModel.OnBountyClickListener onBountyButtonClickListener) {
    this.onBountyButtonClickListener = onBountyButtonClickListener;
  }

  void setOnNewUserWelcomeClickListener(View.OnClickListener onNewUserWelcomeClickListener) {
    this.onNewUserWelcomeClickListener = onNewUserWelcomeClickListener;
  }

  private void createTextPost(PostRealm post) {
    new TextPostModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .callbacks(postCallbacks)
        .detailLayout(false)
        .addTo(this);
  }

  private void createRichPost(PostRealm post) {
    new RichPostModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .callbacks(postCallbacks)
        .detailLayout(false)
        .set(constraintSet)
        .addTo(this);
  }

  private void createBlogPost(PostRealm post) {
    new BlogPostModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .callbacks(postCallbacks)
        .detailLayout(false)
        .addTo(this);
  }

  private void createNewUserWelcomeItem(AccountRealm account) {
    new NewUserWelcomeModel_()
        .id(account.getId())
        .account(account)
        .glide(glide)
        .onClickListener(onNewUserWelcomeClickListener)
        .addTo(this);
  }

  @Override protected void buildModels(FeedPresenter.FeedState state) {
    Stream.of(state.getData()).forEach(item -> {
      if (item instanceof RecommendedGroupListFeedItem) {
        new RecommendedGroupListModelGroup((RecommendedGroupListFeedItem) item,
            recommendedGroupListCallbacks, glide).addTo(this);
      } else if (item instanceof TrendingBlogListFeedItem) {
        new TrendingBlogListModelGroup(((TrendingBlogListFeedItem) item),
            trendingBlogListCallbacks, glide).addTo(this);
      } else if (item instanceof BountyButtonFeedItem) {
        bountyButton.onBountyClickListener(onBountyButtonClickListener).addTo(this);
      } else if (item instanceof TextPostFeedItem) {
        createTextPost(((TextPostFeedItem) item).getItem());
      } else if (item instanceof RichPostFeedItem) {
        createRichPost(((RichPostFeedItem) item).getItem());
      } else if (item instanceof BlogPostFeedItem) {
        createBlogPost(((BlogPostFeedItem) item).getItem());
      } else if (item instanceof NewUserListFeedItem) {
        new NewUserListModelGroup((NewUserListFeedItem) item, newUserListModelGroupCallbacks,
            glide).addIf(!((NewUserListFeedItem) item).getItem().isEmpty(), this);
      } else if (item instanceof NewUserWelcomeFeedItem) {
        createNewUserWelcomeItem(((NewUserWelcomeFeedItem) item).getItem());
      }
    });

    //loader.addIf(state.isLoadingMore(), this);
  }
}
