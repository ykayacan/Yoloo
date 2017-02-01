package com.yoloo.android.feature.feed.common.adapter;

import android.view.View;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
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
import com.yoloo.android.feature.feed.common.model.LoadingModel;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel_;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel_;
import com.yoloo.android.feature.feed.common.model.TrendingCategoryModel_;
import com.yoloo.android.util.WeakHandler;
import java.util.List;

public class FeedAdapter extends EpoxyAdapter {

  private final BountyButtonModel_ bountyButtonModel = new BountyButtonModel_();
  private final TrendingCategoryModel_ trendingCategoriesModel = new TrendingCategoryModel_();
  private final LoadingModel loadingModel = new LoadingModel();

  private final OnProfileClickListener onProfileClickListener;
  private final OnPostOptionsClickListener onPostOptionsClickListener;
  private final OnReadMoreClickListener onReadMoreClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnContentImageClickListener onContentImageClickListener;

  private final boolean isMainFeed;
  private WeakHandler handler = new WeakHandler();

  private FeedAdapter(
      OnProfileClickListener onProfileClickListener,
      OnPostOptionsClickListener onPostOptionsClickListener,
      OnReadMoreClickListener onReadMoreClickListener,
      OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      OnExploreCategoriesClickListener onExploreCategoriesClickListener,
      OnVoteClickListener onVoteClickListener,
      OnBountyClickListener onBountyClickListener,
      OnContentImageClickListener onContentImageClickListener,
      boolean isMainFeed) {
    this.onProfileClickListener = onProfileClickListener;
    this.onPostOptionsClickListener = onPostOptionsClickListener;
    this.onReadMoreClickListener = onReadMoreClickListener;
    this.onShareClickListener = onShareClickListener;
    this.onCommentClickListener = onCommentClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onContentImageClickListener = onContentImageClickListener;
    this.isMainFeed = isMainFeed;

    enableDiffing();

    if (isMainFeed) {
      trendingCategoriesModel.onExploreCategoriesClickListener(onExploreCategoriesClickListener);
      addModel(trendingCategoriesModel);

      bountyButtonModel.onBountyClickListener(onBountyClickListener);
      addModel(bountyButtonModel);
    }
  }

  public static FeedAdapterBuilder builder() {
    return new FeedAdapterBuilder();
  }

  public void addTrendingCategories(List<CategoryRealm> items,
      OnCategoryClickListener onCategoryClickListener) {
    trendingCategoriesModel.addTrendingCategories(items, onCategoryClickListener);
  }

  public void addPostAfterBountyButton(PostRealm post) {
    final int postType = post.getType();

    if (postType == 0) {
      insertModelAfter(createNormalQuestion(post), bountyButtonModel);
    } else if (postType == 1) {
      insertModelAfter(createRichQuestion(post), bountyButtonModel);
    } else if (postType == 2) {
      insertModelAfter(createBlog(post), bountyButtonModel);
    }
  }

  public void addPosts(List<PostRealm> posts) {
    for (PostRealm post : posts) {
      final int postType = post.getType();

      if (postType == 0) {
        models.add(createNormalQuestion(post));
      } else if (postType == 1) {
        models.add(createRichQuestion(post));
      } else if (postType == 2) {
        models.add(createBlog(post));
      }
    }

    notifyModelsChanged();
  }

  public void clear() {
    if (isMainFeed) {
      removeAllAfterModel(bountyButtonModel);
    } else {
      models.clear();
      notifyModelsChanged();
    }
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
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

  public void showLoadMoreIndicator(boolean show) {
    handler.post(() -> {
      if (show) {
        addModel(loadingModel);
      } else {
        removeModel(loadingModel);
      }
    });
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
        .post(post);
  }

  public interface OnBountyClickListener {
    void onBountyClick(View v);
  }

  public interface OnCategoryClickListener {
    void onCategoryClick(View v, String categoryId, String name);
  }

  public interface OnExploreCategoriesClickListener {
    void onExploreCategoriesClick(View v);
  }

  public static class FeedAdapterBuilder {
    private OnProfileClickListener onProfileClickListener;
    private OnPostOptionsClickListener onPostOptionsClickListener;
    private OnReadMoreClickListener onReadMoreClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnExploreCategoriesClickListener onExploreCategoriesClickListener;
    private OnVoteClickListener onVoteClickListener;
    private OnContentImageClickListener onContentImageClickListener;
    private OnBountyClickListener onBountyClickListener;
    private boolean isMainFeed;

    FeedAdapterBuilder() {
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

    public FeedAdapter.FeedAdapterBuilder onExploreCategoriesClickListener(
        OnExploreCategoriesClickListener onExploreCategoriesClickListener) {
      this.onExploreCategoriesClickListener = onExploreCategoriesClickListener;
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
          onExploreCategoriesClickListener, onVoteClickListener,
          onBountyClickListener, onContentImageClickListener, isMainFeed);
    }
  }
}