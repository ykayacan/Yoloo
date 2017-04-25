package com.yoloo.android.feature.postlist;

import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.loading.LoadingModel;
import com.yoloo.android.feature.feed.component.post.BlogModel;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class PostListAdapter extends EpoxyAdapter {

  private final ConstraintSet set;

  private final CropCircleTransformation circleTransformation;

  private final RequestManager glide;

  private String userId;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnItemClickListener<PostRealm> onPostClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnVoteClickListener onVoteClickListener;
  private OnContentImageClickListener onContentImageClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;
  private LoadingModel loadingModel;

  public PostListAdapter(Context context, RequestManager glide) {
    enableDiffing();
    set = new ConstraintSet();

    loadingModel = new LoadingModel();

    circleTransformation = new CropCircleTransformation(context);
    this.glide = glide;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setOnProfileClickListener(OnProfileClickListener onProfileClickListener) {
    this.onProfileClickListener = onProfileClickListener;
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener onPostOptionsClickListener) {
    this.onPostOptionsClickListener = onPostOptionsClickListener;
  }

  public void setOnPostClickListener(OnItemClickListener<PostRealm> onPostClickListener) {
    this.onPostClickListener = onPostClickListener;
  }

  public void setOnShareClickListener(OnShareClickListener onShareClickListener) {
    this.onShareClickListener = onShareClickListener;
  }

  public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener) {
    this.onCommentClickListener = onCommentClickListener;
  }

  public void setOnVoteClickListener(OnVoteClickListener onVoteClickListener) {
    this.onVoteClickListener = onVoteClickListener;
  }

  public void setOnContentImageClickListener(
      OnContentImageClickListener onContentImageClickListener) {
    this.onContentImageClickListener = onContentImageClickListener;
  }

  public void setOnBookmarkClickListener(OnBookmarkClickListener onBookmarkClickListener) {
    this.onBookmarkClickListener = onBookmarkClickListener;
  }

  public void addPosts(List<PostRealm> posts) {
    for (PostRealm post : posts) {
      if (post.isTextQuestionPost()) {
        models.add(createTextQuestion(post));
      } else if (post.isRichQuestionPost()) {
        models.add(createRichQuestion(post));
      } else if (post.isBlogPost()) {
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
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onContentImageClickListener(onContentImageClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .layout(R.layout.item_feed_question_rich)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .set(set)
        .post(post)
        .userId(userId);
  }

  private TextQuestionModel createTextQuestion(PostRealm post) {
    return new TextQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .layout(R.layout.item_feed_question_text)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .post(post)
        .userId(userId);
  }

  private BlogModel createBlog(PostRealm post) {
    return new BlogModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .layout(R.layout.item_feed_blog)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .post(post)
        .userId(userId);
  }
}
