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
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnReadMoreClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.common.model.BountyButtonModel_;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel_;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel_;
import com.yoloo.android.feature.feed.common.model.TrendingCategoryModel_;
import java.util.List;

public class FeedAdapter extends EpoxyAdapter {

  private final BountyButtonModel_ bountyButtonModel = new BountyButtonModel_();
  private final TrendingCategoryModel_ trendingCategoriesModel = new TrendingCategoryModel_();

  private final OnProfileClickListener onProfileClickListener;
  private final OnOptionsClickListener onOptionsClickListener;
  private final OnReadMoreClickListener onReadMoreClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnCategoryClickListener onCategoryClickListener;
  private final OnExploreCategoriesClickListener onExploreCategoriesClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnContentImageClickListener onContentImageClickListener;

  private final boolean isMainFeed;

  private FeedAdapter(OnProfileClickListener onProfileClickListener,
      OnOptionsClickListener onOptionsClickListener,
      OnReadMoreClickListener onReadMoreClickListener, OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      OnCategoryClickListener onCategoryClickListener,
      OnExploreCategoriesClickListener onExploreCategoriesClickListener,
      OnVoteClickListener onVoteClickListener, OnBountyClickListener onBountyClickListener,
      OnContentImageClickListener onContentImageClickListener, boolean isMainFeed) {
    this.onProfileClickListener = onProfileClickListener;
    this.onOptionsClickListener = onOptionsClickListener;
    this.onReadMoreClickListener = onReadMoreClickListener;
    this.onShareClickListener = onShareClickListener;
    this.onCommentClickListener = onCommentClickListener;
    this.onCategoryClickListener = onCategoryClickListener;
    this.onExploreCategoriesClickListener = onExploreCategoriesClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onContentImageClickListener = onContentImageClickListener;
    this.isMainFeed = isMainFeed;

    enableDiffing();

    if (isMainFeed) {
      trendingCategoriesModel.onCategoryClickListener(onCategoryClickListener);
      trendingCategoriesModel.onExploreCategoriesClickListener(onExploreCategoriesClickListener);
      addModel(trendingCategoriesModel);

      bountyButtonModel.onBountyClickListener(onBountyClickListener);
      addModel(bountyButtonModel);
    }
  }

  public static FeedAdapterBuilder builder() {
    return new FeedAdapterBuilder();
  }

  public void addPostToBeginning(PostRealm post) {
    if (post.getType() == 0) {
      insertModelAfter(new NormalQuestionModel_().onProfileClickListener(onProfileClickListener)
          .onOptionsClickListener(onOptionsClickListener)
          .onReadMoreClickListener(onReadMoreClickListener)
          .onShareClickListener(onShareClickListener)
          .onCommentClickListener(onCommentClickListener)
          .onVoteClickListener(onVoteClickListener)
          .layout(R.layout.item_question_normal)
          .post(post), bountyButtonModel);
    } else if (post.getType() == 1) {
      insertModelAfter(new RichQuestionModel_().onProfileClickListener(onProfileClickListener)
          .onOptionsClickListener(onOptionsClickListener)
          .onReadMoreClickListener(onReadMoreClickListener)
          .onShareClickListener(onShareClickListener)
          .onCommentClickListener(onCommentClickListener)
          .onVoteClickListener(onVoteClickListener)
          .onContentImageClickListener(onContentImageClickListener)
          .layout(R.layout.item_question_rich)
          .post(post), bountyButtonModel);
    }
  }

  public void addPosts(List<PostRealm> posts) {
    for (PostRealm post : posts) {
      if (post.getType() == 0) {
        models.add(new NormalQuestionModel_().onProfileClickListener(onProfileClickListener)
            .onOptionsClickListener(onOptionsClickListener)
            .onReadMoreClickListener(onReadMoreClickListener)
            .onShareClickListener(onShareClickListener)
            .onCommentClickListener(onCommentClickListener)
            .onVoteClickListener(onVoteClickListener)
            .layout(R.layout.item_question_normal)
            .post(post));
      } else if (post.getType() == 1) {
        models.add(new RichQuestionModel_().onProfileClickListener(onProfileClickListener)
            .onOptionsClickListener(onOptionsClickListener)
            .onReadMoreClickListener(onReadMoreClickListener)
            .onShareClickListener(onShareClickListener)
            .onCommentClickListener(onCommentClickListener)
            .onVoteClickListener(onVoteClickListener)
            .onContentImageClickListener(onContentImageClickListener)
            .layout(R.layout.item_question_rich)
            .post(post));
      }
    }

    notifyModelsChanged();
  }

  public void updateTrendingCategories(List<CategoryRealm> items) {
    trendingCategoriesModel.updateTrendingCategories(items);
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

  public void update(String itemId, @FeedAction int action, Object payload) {
    if (action == FeedAction.UNSPECIFIED) {
      return;
    }

    for (final EpoxyModel<?> model : models) {
      if (model instanceof NormalQuestionModel) {
        if (((NormalQuestionModel) model).getItemId().equals(itemId)) {
          setAction(action, payload, model);
          break;
        }
      } else if (model instanceof RichQuestionModel) {
        if (((RichQuestionModel) model).getItemId().equals(itemId)) {
          setAction(action, payload, model);
          break;
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
    private OnOptionsClickListener onOptionsClickListener;
    private OnReadMoreClickListener onReadMoreClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnCategoryClickListener onCategoryClickListener;
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
        OnOptionsClickListener onOptionsClickListener) {
      this.onOptionsClickListener = onOptionsClickListener;
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

    public FeedAdapter.FeedAdapterBuilder onCategoryClickListener(
        OnCategoryClickListener onCategoryClickListener) {
      this.onCategoryClickListener = onCategoryClickListener;
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
      return new FeedAdapter(onProfileClickListener, onOptionsClickListener,
          onReadMoreClickListener, onShareClickListener, onCommentClickListener,
          onCategoryClickListener, onExploreCategoriesClickListener, onVoteClickListener,
          onBountyClickListener, onContentImageClickListener, isMainFeed);
    }
  }
}