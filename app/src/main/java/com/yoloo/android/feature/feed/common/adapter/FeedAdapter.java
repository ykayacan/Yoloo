package com.yoloo.android.feature.feed.common.adapter;

import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
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
import com.yoloo.android.feature.feed.common.model.BlogModel;
import com.yoloo.android.feature.feed.common.model.BlogModel_;
import com.yoloo.android.feature.feed.common.model.BountyButtonModel_;
import com.yoloo.android.feature.feed.common.model.FeedNewsModel_;
import com.yoloo.android.feature.feed.common.model.FeedTrendingCategoryModel_;
import com.yoloo.android.feature.feed.common.model.LoadingModel;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel_;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;
import timber.log.Timber;

public class FeedAdapter extends EpoxyAdapter {

  private final LoadingModel loadingModel;

  private final OnProfileClickListener onProfileClickListener;
  private final OnPostOptionsClickListener onPostOptionsClickListener;
  private final OnReadMoreClickListener onReadMoreClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnContentImageClickListener onContentImageClickListener;
  private final boolean isMainFeed;

  private final ConstraintSet set = new ConstraintSet();

  private final CropCircleTransformation circleTransformation;

  private FeedNewsModel_ newsModel;
  private BountyButtonModel_ bountyButtonModel;
  private FeedTrendingCategoryModel_ trendingCategoryModel;

  private FeedAdapter(
      OnProfileClickListener onProfileClickListener,
      OnPostOptionsClickListener onPostOptionsClickListener,
      OnReadMoreClickListener onReadMoreClickListener,
      OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      View.OnClickListener trendingCategoryHeaderClickListener,
      View.OnClickListener travelNewsHeaderClickListener,
      OnVoteClickListener onVoteClickListener,
      OnBountyClickListener onBountyClickListener,
      OnContentImageClickListener onContentImageClickListener,
      boolean isMainFeed, Context context) {
    this.onProfileClickListener = onProfileClickListener;
    this.onPostOptionsClickListener = onPostOptionsClickListener;
    this.onReadMoreClickListener = onReadMoreClickListener;
    this.onShareClickListener = onShareClickListener;
    this.onCommentClickListener = onCommentClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onContentImageClickListener = onContentImageClickListener;
    this.isMainFeed = isMainFeed;

    enableDiffing();

    loadingModel = new LoadingModel();

    if (isMainFeed) {
      bountyButtonModel = new BountyButtonModel_();
      trendingCategoryModel = new FeedTrendingCategoryModel_(context);
      newsModel = new FeedNewsModel_(context);

      bountyButtonModel.onBountyClickListener(onBountyClickListener);
      trendingCategoryModel.headerClickListener(trendingCategoryHeaderClickListener);
      newsModel.headerClickListener(travelNewsHeaderClickListener);
    }

    circleTransformation = new CropCircleTransformation(context);
  }

  public static FeedAdapterBuilder builder(Context context) {
    return new FeedAdapterBuilder(context);
  }

  public void addTrendingCategories(List<CategoryRealm> items,
      OnItemClickListener<CategoryRealm> onItemClickListener) {
    trendingCategoryModel.addTrendingCategories(items, onItemClickListener);
    addModel(trendingCategoryModel);
  }

  public void addNews(List<NewsRealm> newsList,
      OnItemClickListener<NewsRealm> onItemClickListener) {
    Timber.d("addNews()");
    newsModel.addNews(newsList, onItemClickListener);
    insertModelAfter(newsModel, trendingCategoryModel);
    insertModelAfter(bountyButtonModel, newsModel);
  }

  public void addPostToBeginning(PostRealm post) {
    final int postType = post.getType();

    if (postType == PostRealm.POST_NORMAL) {
      insertModelAfter(createNormalQuestion(post), bountyButtonModel);
    } else if (postType == PostRealm.POST_RICH) {
      insertModelAfter(createRichQuestion(post), bountyButtonModel);
    } else if (postType == PostRealm.POST_BLOG) {
      insertModelAfter(createBlog(post), bountyButtonModel);
    }
  }

  public void addPosts(List<PostRealm> posts) {
    Timber.d("addPosts(): %s", posts.size());
    for (PostRealm post : posts) {
      final int postType = post.getType();

      if (postType == PostRealm.POST_NORMAL) {
        models.add(createNormalQuestion(post));
      } else if (postType == PostRealm.POST_RICH) {
        models.add(createRichQuestion(post));
      } else if (postType == PostRealm.POST_BLOG) {
        models.add(createBlog(post));
      }
    }

    notifyModelsChanged();
  }

  public void clear() {
    if (isMainFeed) {
      models.subList(3, models.size() - 1).clear();
    } else {
      models.clear();
    }
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public void showFooter(RecyclerView recyclerView, boolean show) {
    /*if (show) {
      Timber.d("showFooter(%s)", true);
      recyclerView.post(() -> addModel(loadingModel));
    } else {
      Timber.d("showFooter(%s)", false);
      recyclerView.post(() -> removeModel(loadingModel));
    }*/
  }

  public void updatePost(@FeedAction int action, PostRealm payload) {
    for (EpoxyModel<?> model : models) {
      if (model instanceof NormalQuestionModel) {
        if (((NormalQuestionModel) model).getItemId().equals(payload.getId())) {
          setAction(action, payload, model);
          break;
        }
      } else if (model instanceof RichQuestionModel) {
        if (((RichQuestionModel) model).getItemId().equals(payload.getId())) {
          setAction(action, payload, model);
          break;
        }
      } else if (model instanceof BlogModel) {
        if (((BlogModel) model).getItemId().equals(payload.getId())) {
          setAction(action, payload, model);
        }
      }
    }
  }

  private void setAction(@FeedAction int action, Object payload, EpoxyModel<?> model) {
    if (action == FeedAction.DELETE) {
      removeModel(model);
    } else if (action == FeedAction.UPDATE) {
      notifyModelChanged(model, payload);
    }
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

  private NormalQuestionModel createNormalQuestion(PostRealm post) {
    return new NormalQuestionModel_()
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

  public interface OnBountyClickListener {
    void onBountyClick(View v);
  }

  public static class FeedAdapterBuilder {
    private final Context context;
    private OnProfileClickListener onProfileClickListener;
    private OnPostOptionsClickListener onPostOptionsClickListener;
    private OnReadMoreClickListener onReadMoreClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private View.OnClickListener trendingCategoryHeaderClickListener;
    private View.OnClickListener travelNewsHeaderClickListener;
    private OnVoteClickListener onVoteClickListener;
    private OnContentImageClickListener onContentImageClickListener;
    private OnBountyClickListener onBountyClickListener;
    private boolean isMainFeed;

    FeedAdapterBuilder(Context context) {
      this.context = context;
    }

    public FeedAdapter.FeedAdapterBuilder onProfileClickListener(
        OnProfileClickListener onProfileClickListener) {
      this.onProfileClickListener = onProfileClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onOptionsClickListener(
        OnPostOptionsClickListener onPostOptionsClickListener) {
      this.onPostOptionsClickListener = onPostOptionsClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onReadMoreClickListener(
        OnReadMoreClickListener onReadMoreClickListener) {
      this.onReadMoreClickListener = onReadMoreClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onShareClickListener(
        OnShareClickListener onShareClickListener) {
      this.onShareClickListener = onShareClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onCommentClickListener(
        OnCommentClickListener onCommentClickListener) {
      this.onCommentClickListener = onCommentClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onTrendingCategoryHeaderClickListener(
        View.OnClickListener trendingCategoryHeaderClickListener) {
      this.trendingCategoryHeaderClickListener = trendingCategoryHeaderClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onTravelNewsHeaderClickListener(
        View.OnClickListener travelNewsHeaderClickListener) {
      this.travelNewsHeaderClickListener = travelNewsHeaderClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onVoteClickListener(
        OnVoteClickListener onVoteClickListener) {
      this.onVoteClickListener = onVoteClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onContentImageClickListener(
        OnContentImageClickListener onContentImageClickListener) {
      this.onContentImageClickListener = onContentImageClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder onBountyClickListener(
        OnBountyClickListener onBountyClickListener) {
      this.onBountyClickListener = onBountyClickListener;
      return this;
    }

    public FeedAdapter.FeedAdapterBuilder isMainFeed(boolean isMainFeed) {
      this.isMainFeed = isMainFeed;
      return this;
    }

    public FeedAdapter build() {
      return new FeedAdapter(onProfileClickListener, onPostOptionsClickListener,
          onReadMoreClickListener, onShareClickListener, onCommentClickListener,
          trendingCategoryHeaderClickListener, travelNewsHeaderClickListener, onVoteClickListener,
          onBountyClickListener, onContentImageClickListener, isMainFeed, context);
    }
  }
}
