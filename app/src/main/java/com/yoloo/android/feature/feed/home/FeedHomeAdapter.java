package com.yoloo.android.feature.feed.home;

import android.content.Context;
import android.support.constraint.ConstraintSet;
import android.view.View;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.BlogFeedItem;
import com.yoloo.android.data.feedtypes.BountyButtonFeedItem;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.feedtypes.NewUsersFeedItem;
import com.yoloo.android.data.feedtypes.RichQuestionFeedItem;
import com.yoloo.android.data.feedtypes.TextQuestionFeedItem;
import com.yoloo.android.data.feedtypes.TrendingBlogsFeedItem;
import com.yoloo.android.data.feedtypes.TrendingCategoriesFeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.bountybutton.BountyButtonModel_;
import com.yoloo.android.feature.feed.component.loading.LoadingModel;
import com.yoloo.android.feature.feed.component.newcomers.NewcomersContactAdapter;
import com.yoloo.android.feature.feed.component.newcomers.NewcomersModel_;
import com.yoloo.android.feature.feed.component.post.BlogModel;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.feature.feed.component.trendingblogs.FeedTrendingBlogsModel_;
import com.yoloo.android.feature.feed.component.trendingcategory.FeedTrendingCategoryModel_;
import com.yoloo.android.feature.search.OnFollowClickListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class FeedHomeAdapter extends EpoxyAdapter {

  private final ConstraintSet set = new ConstraintSet();
  private final CropCircleTransformation circleTransformation;
  private final LoadingModel loadingModel;
  private final FeedTrendingBlogsModel_ trendingBlogsModel;
  private final BountyButtonModel_ bountyButtonModel;
  private final FeedTrendingCategoryModel_ trendingCategoryModel;
  private final NewcomersModel_ newcomersModel;

  private final RequestManager glide;

  private String userId;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;
  private OnItemClickListener<PostRealm> onPostClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnVoteClickListener onVoteClickListener;
  private OnContentImageClickListener onContentImageClickListener;

  public FeedHomeAdapter(Context context, RequestManager glide) {
    enableDiffing();

    loadingModel = new LoadingModel();
    bountyButtonModel = new BountyButtonModel_();
    trendingCategoryModel = new FeedTrendingCategoryModel_(context, glide);
    trendingBlogsModel = new FeedTrendingBlogsModel_(context, glide);
    newcomersModel = new NewcomersModel_(context);

    circleTransformation = new CropCircleTransformation(context);
    this.glide = glide;
  }

  public void setUserId(String userId) {
    this.userId = userId;
    trendingBlogsModel.setUserId(userId);
  }

  public void setOnRecommendedGroupHeaderClickListener(View.OnClickListener listener) {
    trendingCategoryModel.onHeaderClickListener(listener);
  }

  public void setOnRecommendedGroupItemClickListener(OnItemClickListener<GroupRealm> listener) {
    trendingCategoryModel.setOnItemClickListener(listener);
  }

  public void setOnTrendingBlogHeaderClickListener(View.OnClickListener listener) {
    trendingBlogsModel.onHeaderClickListener(listener);
  }

  public void setOnTrendingBlogItemClickListener(OnItemClickListener<PostRealm> listener) {
    trendingBlogsModel.setOnItemClickListener(listener);
  }

  public void setOnBountyButtonClickListener(View.OnClickListener listener) {
    bountyButtonModel.onClickListener(listener);
  }

  public void setOnProfileClickListener(OnProfileClickListener listener) {
    this.onProfileClickListener = listener;
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener listener) {
    this.onPostOptionsClickListener = listener;
    trendingBlogsModel.setOnPostOptionsClickListener(listener);
  }

  public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
    this.onBookmarkClickListener = listener;
    trendingBlogsModel.setOnBookmarkClickListener(listener);
  }

  public void setOnPostClickListener(OnItemClickListener<PostRealm> listener) {
    this.onPostClickListener = listener;
  }

  public void setOnShareClickListener(OnShareClickListener listener) {
    this.onShareClickListener = listener;
  }

  public void setOnCommentClickListener(OnCommentClickListener listener) {
    this.onCommentClickListener = listener;
  }

  public void setOnVoteClickListener(OnVoteClickListener listener) {
    this.onVoteClickListener = listener;
  }

  public void setOnContentImageClickListener(OnContentImageClickListener listener) {
    this.onContentImageClickListener = listener;
  }

  public void setOnNewcomersHeaderClickListener(View.OnClickListener listener) {
    newcomersModel.onHeaderClickListener(listener);
  }

  public void setOnNewcomersItemClickListener(OnItemClickListener<AccountRealm> listener) {
    newcomersModel.setOnItemClickListener(listener);
  }

  public void setOnNewcomersFollowClickListener(OnFollowClickListener listener) {
    newcomersModel.setOnFollowClickListener(listener);
  }

  void addPostToBeginning(PostRealm post) {
    final int postType = post.getPostType();

    if (postType == PostRealm.TYPE_TEXT) {
      insertModelAfter(createTextQuestion(post), bountyButtonModel);
    } else if (postType == PostRealm.TYPE_RICH) {
      insertModelAfter(createRichQuestion(post), bountyButtonModel);
    } else if (postType == PostRealm.TYPE_BLOG) {
      insertModelAfter(createBlog(post), bountyButtonModel);
    }
  }

  void addFeedItems(List<? extends FeedItem> items) {
    for (FeedItem item : items) {
      if (item instanceof TrendingCategoriesFeedItem) {
        trendingCategoryModel.addTrendingCategories(
            ((TrendingCategoriesFeedItem) item).getCategories());
        models.add(trendingCategoryModel);
      } else if (item instanceof TrendingBlogsFeedItem) {
        trendingBlogsModel.addTrendingBlogs(((TrendingBlogsFeedItem) item).getTrendingBlogs());
        models.add(trendingBlogsModel);
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

  void clearPostsSection() {
    if (models.size() > 3) {
      models.subList(3, models.size()).clear();
    }
  }

  void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  void deleteNewcomersModel(EpoxyModel<?> model) {
    final NewcomersContactAdapter adapter = newcomersModel.getAdapter();
    adapter.delete(model);

    if (adapter.getItemCount() == 0) {
      removeModel(newcomersModel);
    }
  }

  void updatePost(@FeedAction int action, PostRealm payload) {
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
    return new TextQuestionModel_().id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_text)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .post(post);
  }

  private RichQuestionModel createRichQuestion(PostRealm post) {
    return new RichQuestionModel_().id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onContentImageClickListener(onContentImageClickListener)
        .layout(R.layout.item_feed_question_rich)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .set(set)
        .post(post);
  }

  private BlogModel createBlog(PostRealm post) {
    return new BlogModel_().id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .circleTransformation(circleTransformation)
        .glide(glide)
        .post(post);
  }
}
