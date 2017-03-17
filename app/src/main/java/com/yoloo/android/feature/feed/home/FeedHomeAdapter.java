package com.yoloo.android.feature.feed.home;

import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.view.View;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.BlogFeedItem;
import com.yoloo.android.data.feedtypes.BountyButtonFeedItem;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.feedtypes.NewUsersFeedItem;
import com.yoloo.android.data.feedtypes.RichQuestionFeedItem;
import com.yoloo.android.data.feedtypes.TextQuestionFeedItem;
import com.yoloo.android.data.feedtypes.TravelNewsFeedItem;
import com.yoloo.android.data.feedtypes.TrendingCategoriesFeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.blog.BlogModel;
import com.yoloo.android.feature.feed.component.blog.BlogModel_;
import com.yoloo.android.feature.feed.component.bountybutton.BountyButtonModel;
import com.yoloo.android.feature.feed.component.bountybutton.BountyButtonModel_;
import com.yoloo.android.feature.feed.component.loading.LoadingModel;
import com.yoloo.android.feature.feed.component.newcomers.NewcomersContactAdapter;
import com.yoloo.android.feature.feed.component.newcomers.NewcomersModel_;
import com.yoloo.android.feature.feed.component.news.FeedNewsModel_;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.feature.feed.component.trendingcategory.FeedTrendingCategoryModel_;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class FeedHomeAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnPostOptionsClickListener onPostOptionsClickListener;
  private final OnReadMoreClickListener onReadMoreClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnContentImageClickListener onContentImageClickListener;
  private final OnItemClickListener<CategoryRealm> onCategoryItemClickListener;
  private final OnItemClickListener<NewsRealm> onNewsItemClickListener;

  private final ConstraintSet set = new ConstraintSet();

  private final CropCircleTransformation circleTransformation;

  private final LoadingModel loadingModel;
  private final FeedNewsModel_ newsModel;
  private final BountyButtonModel_ bountyButtonModel;
  private final FeedTrendingCategoryModel_ trendingCategoryModel;
  private final NewcomersModel_ newcomersModel;

  private FeedHomeAdapter(FeedBuilder builder) {
    this.onProfileClickListener = builder.onProfileClickListener;
    this.onPostOptionsClickListener = builder.onPostOptionsClickListener;
    this.onReadMoreClickListener = builder.onReadMoreClickListener;
    this.onShareClickListener = builder.onShareClickListener;
    this.onCommentClickListener = builder.onCommentClickListener;
    this.onVoteClickListener = builder.onVoteClickListener;
    this.onContentImageClickListener = builder.onContentImageClickListener;
    this.onCategoryItemClickListener = builder.onTrendingCategoryItemClickListener;
    this.onNewsItemClickListener = builder.onNewsItemClickListener;

    final Context context = builder.context;

    enableDiffing();

    loadingModel = new LoadingModel();
    bountyButtonModel = new BountyButtonModel_();
    trendingCategoryModel = new FeedTrendingCategoryModel_(context);
    newsModel = new FeedNewsModel_(context);
    newcomersModel = new NewcomersModel_(context, builder.onNewcomersFollowClickListener,
        builder.onNewcomersItemClickListener);
    newcomersModel.moreClickListener(builder.onNewcomersHeaderClickListener);

    bountyButtonModel.onBountyButtonClickListener(builder.onBountyButtonClickListener);
    trendingCategoryModel.headerClickListener(builder.onTrendingCategoryHeaderClickListener);
    newsModel.headerClickListener(builder.onNewsHeaderClickListener);

    circleTransformation = new CropCircleTransformation(context);
  }

  public static FeedBuilder builder(Context context) {
    return new FeedBuilder(context);
  }

  public void addPostToBeginning(PostRealm post) {
    final int postType = post.getPostType();

    if (postType == PostRealm.POST_TEXT) {
      insertModelAfter(createTextQuestion(post), bountyButtonModel);
    } else if (postType == PostRealm.POST_RICH) {
      insertModelAfter(createRichQuestion(post), bountyButtonModel);
    } else if (postType == PostRealm.POST_BLOG) {
      insertModelAfter(createBlog(post), bountyButtonModel);
    }
  }

  public void addFeedItems(List<? extends FeedItem> items) {
    for (FeedItem item : items) {
      if (item instanceof TrendingCategoriesFeedItem) {
        trendingCategoryModel.addTrendingCategories(
            ((TrendingCategoriesFeedItem) item).getCategories(), onCategoryItemClickListener);
        models.add(trendingCategoryModel);
      } else if (item instanceof TravelNewsFeedItem) {
        newsModel.addNews(((TravelNewsFeedItem) item).getNews(), onNewsItemClickListener);
        models.add(newsModel);
      } else if (item instanceof BountyButtonFeedItem) {
        models.add(bountyButtonModel);
      } else if (item instanceof TextQuestionFeedItem) {
        models.add(createTextQuestion(((TextQuestionFeedItem) item).getPost()));
      } else if (item instanceof RichQuestionFeedItem) {
        models.add(createRichQuestion(((RichQuestionFeedItem) item).getPost()));
      } else if (item instanceof BlogFeedItem) {
        models.add(createBlog(((BlogFeedItem) item).getPost()));
      } else if (item instanceof NewUsersFeedItem) {
        newcomersModel.addNewcomersContacts(((NewUsersFeedItem) item).getUsers());
        models.add(newcomersModel);
      }
    }

    notifyModelsChanged();
  }

  public void clearPostsSection() {
    if (models.size() > 3) {
      models.subList(3, models.size()).clear();
    }
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public void deleteNewcomersModel(EpoxyModel<?> model) {
    final NewcomersContactAdapter adapter = newcomersModel.getAdapter();
    adapter.delete(model);

    if (adapter.getItemCount() == 0) {
      removeModel(newcomersModel);
    }
  }

  public void updatePost(@FeedAction int action, PostRealm payload) {
    EpoxyModel<?> modelToBeUpdated = null;
    for (EpoxyModel<?> model : models) {
      if (model instanceof TextQuestionModel) {
        if (((TextQuestionModel) model).getItemId().equals(payload.getId())) {
          modelToBeUpdated = model;
          break;
        }
      } else if (model instanceof RichQuestionModel) {
        if (((RichQuestionModel) model).getItemId().equals(payload.getId())) {
          modelToBeUpdated = model;
          break;
        }
      } else if (model instanceof BlogModel) {
        if (((BlogModel) model).getItemId().equals(payload.getId())) {
          modelToBeUpdated = model;
          break;
        }
      }
    }
    setAction(action, payload, modelToBeUpdated);
  }

  private void setAction(int action, Object payload, EpoxyModel<?> model) {
    if (action == FeedAction.DELETE) {
      removeModel(model);
    } else if (action == FeedAction.UPDATE) {
      notifyModelChanged(model, payload);
    }
  }

  private TextQuestionModel createTextQuestion(PostRealm post) {
    return new TextQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onReadMoreClickListener(onReadMoreClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_normal)
        .circleTransformation(circleTransformation)
        .post(post);
  }

  private RichQuestionModel createRichQuestion(PostRealm post) {
    return new RichQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onReadMoreClickListener(onReadMoreClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onContentImageClickListener(onContentImageClickListener)
        .layout(R.layout.item_feed_question_rich)
        .circleTransformation(circleTransformation)
        .set(set)
        .post(post);
  }

  private BlogModel createBlog(PostRealm post) {
    return new BlogModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onReadMoreClickListener(onReadMoreClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .circleTransformation(circleTransformation)
        .post(post);
  }

  public static class FeedBuilder {
    final Context context;
    OnProfileClickListener onProfileClickListener;
    OnPostOptionsClickListener onPostOptionsClickListener;
    OnReadMoreClickListener onReadMoreClickListener;
    OnShareClickListener onShareClickListener;
    OnCommentClickListener onCommentClickListener;
    View.OnClickListener onTrendingCategoryHeaderClickListener;
    OnItemClickListener<CategoryRealm> onTrendingCategoryItemClickListener;
    View.OnClickListener onNewsHeaderClickListener;
    OnItemClickListener<NewsRealm> onNewsItemClickListener;
    OnVoteClickListener onVoteClickListener;
    OnContentImageClickListener onContentImageClickListener;
    BountyButtonModel.OnBountyButtonClickListener onBountyButtonClickListener;
    OnItemClickListener<AccountRealm> onNewcomersItemClickListener;
    OnFollowClickListener onNewcomersFollowClickListener;
    View.OnClickListener onNewcomersHeaderClickListener;

    FeedBuilder(Context context) {
      this.context = context;
    }

    public FeedBuilder onProfileClickListener(
        OnProfileClickListener onProfileClickListener) {
      this.onProfileClickListener = onProfileClickListener;
      return this;
    }

    public FeedBuilder onOptionsClickListener(
        OnPostOptionsClickListener onPostOptionsClickListener) {
      this.onPostOptionsClickListener = onPostOptionsClickListener;
      return this;
    }

    public FeedBuilder onReadMoreClickListener(
        OnReadMoreClickListener onReadMoreClickListener) {
      this.onReadMoreClickListener = onReadMoreClickListener;
      return this;
    }

    public FeedBuilder onShareClickListener(
        OnShareClickListener onShareClickListener) {
      this.onShareClickListener = onShareClickListener;
      return this;
    }

    public FeedBuilder onCommentClickListener(
        OnCommentClickListener onCommentClickListener) {
      this.onCommentClickListener = onCommentClickListener;
      return this;
    }

    public FeedBuilder onTrendingCategoryHeaderClickListener(
        View.OnClickListener onTrendingCategoryHeaderClickListener) {
      this.onTrendingCategoryHeaderClickListener = onTrendingCategoryHeaderClickListener;
      return this;
    }

    public FeedBuilder onTrendingCategoryItemClickListener(
        OnItemClickListener<CategoryRealm> onTrendingCategoryItemClickListener) {
      this.onTrendingCategoryItemClickListener = onTrendingCategoryItemClickListener;
      return this;
    }

    public FeedBuilder onVoteClickListener(
        OnVoteClickListener onVoteClickListener) {
      this.onVoteClickListener = onVoteClickListener;
      return this;
    }

    public FeedBuilder onContentImageClickListener(
        OnContentImageClickListener onContentImageClickListener) {
      this.onContentImageClickListener = onContentImageClickListener;
      return this;
    }

    public FeedBuilder onBountyButtonClickListener(
        BountyButtonModel.OnBountyButtonClickListener onBountyButtonClickListener) {
      this.onBountyButtonClickListener = onBountyButtonClickListener;
      return this;
    }

    public FeedBuilder onNewsHeaderClickListener(
        View.OnClickListener onTravelNewsHeaderClickListener) {
      this.onNewsHeaderClickListener = onTravelNewsHeaderClickListener;
      return this;
    }

    public FeedBuilder onNewsItemClickListener(
        OnItemClickListener<NewsRealm> onNewsItemClickListener) {
      this.onNewsItemClickListener = onNewsItemClickListener;
      return this;
    }

    public FeedBuilder onNewcomersItemClickListener(
        OnItemClickListener<AccountRealm> onNewcomersItemClickListener) {
      this.onNewcomersItemClickListener = onNewcomersItemClickListener;
      return this;
    }

    public FeedBuilder onNewcomersFollowClickListener(
        OnFollowClickListener onNewcomersFollowClickListener) {
      this.onNewcomersFollowClickListener = onNewcomersFollowClickListener;
      return this;
    }

    public FeedBuilder onNewcomersHeaderClickListener(
        View.OnClickListener onNewcomersHeaderClickListener) {
      this.onNewcomersHeaderClickListener = onNewcomersHeaderClickListener;
      return this;
    }

    public FeedHomeAdapter build() {
      return new FeedHomeAdapter(this);
    }
  }
}
