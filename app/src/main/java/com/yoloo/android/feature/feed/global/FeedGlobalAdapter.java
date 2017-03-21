package com.yoloo.android.feature.feed.global;

import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
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
import com.yoloo.android.feature.feed.component.loading.LoadingModel;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import java.util.List;

public class FeedGlobalAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnPostOptionsClickListener onPostOptionsClickListener;
  private final OnReadMoreClickListener onReadMoreClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnContentImageClickListener onContentImageClickListener;

  private final ConstraintSet set = new ConstraintSet();

  private final CropCircleTransformation circleTransformation;

  private LoadingModel loadingModel;

  private FeedGlobalAdapter(FeedAdapterBuilder builder) {
    this.onProfileClickListener = builder.onProfileClickListener;
    this.onPostOptionsClickListener = builder.onPostOptionsClickListener;
    this.onReadMoreClickListener = builder.onReadMoreClickListener;
    this.onShareClickListener = builder.onShareClickListener;
    this.onCommentClickListener = builder.onCommentClickListener;
    this.onVoteClickListener = builder.onVoteClickListener;
    this.onContentImageClickListener = builder.onContentImageClickListener;

    final Context context = builder.context;

    enableDiffing();

    loadingModel = new LoadingModel();

    circleTransformation = new CropCircleTransformation(context);
  }

  public static FeedAdapterBuilder builder(Context context) {
    return new FeedAdapterBuilder(context);
  }

  public void addPosts(List<PostRealm> posts) {
    for (PostRealm post : posts) {
      final int postType = post.getPostType();

      if (postType == PostRealm.POST_TEXT) {
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
    models.clear();
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

  private TextQuestionModel createNormalQuestion(PostRealm post) {
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

  public static class FeedAdapterBuilder {
    final Context context;
    OnProfileClickListener onProfileClickListener;
    OnPostOptionsClickListener onPostOptionsClickListener;
    OnReadMoreClickListener onReadMoreClickListener;
    OnShareClickListener onShareClickListener;
    OnCommentClickListener onCommentClickListener;
    OnVoteClickListener onVoteClickListener;
    OnContentImageClickListener onContentImageClickListener;

    FeedAdapterBuilder(Context context) {
      this.context = context;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onProfileClickListener(
        OnProfileClickListener onProfileClickListener) {
      this.onProfileClickListener = onProfileClickListener;
      return this;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onOptionsClickListener(
        OnPostOptionsClickListener onPostOptionsClickListener) {
      this.onPostOptionsClickListener = onPostOptionsClickListener;
      return this;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onReadMoreClickListener(
        OnReadMoreClickListener onReadMoreClickListener) {
      this.onReadMoreClickListener = onReadMoreClickListener;
      return this;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onShareClickListener(
        OnShareClickListener onShareClickListener) {
      this.onShareClickListener = onShareClickListener;
      return this;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onCommentClickListener(
        OnCommentClickListener onCommentClickListener) {
      this.onCommentClickListener = onCommentClickListener;
      return this;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onVoteClickListener(
        OnVoteClickListener onVoteClickListener) {
      this.onVoteClickListener = onVoteClickListener;
      return this;
    }

    public FeedGlobalAdapter.FeedAdapterBuilder onContentImageClickListener(
        OnContentImageClickListener onContentImageClickListener) {
      this.onContentImageClickListener = onContentImageClickListener;
      return this;
    }

    public FeedGlobalAdapter build() {
      return new FeedGlobalAdapter(this);
    }
  }
}
