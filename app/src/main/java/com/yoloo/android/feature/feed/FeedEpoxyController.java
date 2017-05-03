package com.yoloo.android.feature.feed;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintSet;
import android.view.View;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.yoloo.android.R;
import com.yoloo.android.data.feedtypes.BlogItem;
import com.yoloo.android.data.feedtypes.BountyButtonItem;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.feedtypes.NewUsersFeedItem;
import com.yoloo.android.data.feedtypes.RecommendedGroupListItem;
import com.yoloo.android.data.feedtypes.RichQuestionItem;
import com.yoloo.android.data.feedtypes.TextQuestionItem;
import com.yoloo.android.data.feedtypes.TrendingBlogListItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.bountybutton.BountyButtonModel_;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.newusers.NewUserListModelGroup;
import com.yoloo.android.feature.models.recommendedgroups.RecommendedGroupListModelGroup;
import com.yoloo.android.feature.models.trendingblogs.TrendingBlogListModelGroup;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.util.Preconditions;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

public class FeedEpoxyController extends Typed2EpoxyController<List<FeedItem>, Boolean> {

  private final RequestManager glide;
  private final Transformation<Bitmap> bitmapTransformation;
  private final ConstraintSet constraintSet;

  @AutoModel BountyButtonModel_ bountyButton;
  @AutoModel LoaderModel loader;

  private List<FeedItem> feedItems;
  private View.OnClickListener onBountyButtonClickListener;

  private RecommendedGroupListCallbacks recommendedGroupListCallbacks;
  private TrendingBlogListCallbacks trendingBlogListCallbacks;
  private NewUserListModelGroup.NewUserListModelGroupCallbacks newUserListModelGroupCallbacks;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;
  private OnItemClickListener<PostRealm> onPostClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnVoteClickListener onVoteClickListener;
  private OnContentImageClickListener onContentImageClickListener;

  private String userId;

  public FeedEpoxyController(RequestManager glide, Context context) {
    this.glide = glide;
    this.bitmapTransformation = new CropCircleTransformation(context);
    this.constraintSet = new ConstraintSet();
    this.feedItems = new ArrayList<>();
    setDebugLoggingEnabled(true);
  }

  @Override
  public void setData(List<FeedItem> items, Boolean loadingMore) {
    this.feedItems = items;
    super.setData(items, Preconditions.checkNotNull(loadingMore, "loadingMore cannot be null."));
  }

  @Override
  protected void buildModels(List<FeedItem> feedItems, Boolean loadingMore) {
    Stream.of(feedItems).forEach(item -> {
      if (item instanceof RecommendedGroupListItem) {
        new RecommendedGroupListModelGroup((RecommendedGroupListItem) item,
            recommendedGroupListCallbacks, glide).addTo(this);
      } else if (item instanceof TrendingBlogListItem) {
        new TrendingBlogListModelGroup(userId, ((TrendingBlogListItem) item),
            trendingBlogListCallbacks, glide, bitmapTransformation).addTo(this);
      } else if (item instanceof BountyButtonItem) {
        bountyButton.onClickListener(onBountyButtonClickListener).addTo(this);
      } else if (item instanceof TextQuestionItem) {
        createTextQuestion(((TextQuestionItem) item).getPost());
      } else if (item instanceof RichQuestionItem) {
        createRichQuestion(((RichQuestionItem) item).getPost());
      } else if (item instanceof BlogItem) {
        createBlog(((BlogItem) item).getPost());
      } else if (item instanceof NewUsersFeedItem) {
        new NewUserListModelGroup((NewUsersFeedItem) item, newUserListModelGroupCallbacks,
            glide).addIf(!((NewUsersFeedItem) item).getUsers().isEmpty(), this);
      }
    });

    loader.addIf(loadingMore, this);
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setRecommendedGroupListCallbacks(RecommendedGroupListCallbacks callbacks) {
    this.recommendedGroupListCallbacks = callbacks;
  }

  public void setTrendingBlogListCallbacks(TrendingBlogListCallbacks callbacks) {
    this.trendingBlogListCallbacks = callbacks;
  }

  public void setOnBountyButtonClickListener(View.OnClickListener listener) {
    onBountyButtonClickListener = listener;
  }

  public void setOnProfileClickListener(OnProfileClickListener listener) {
    this.onProfileClickListener = listener;
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener listener) {
    this.onPostOptionsClickListener = listener;
  }

  public void setOnBookmarkClickListener(OnBookmarkClickListener listener) {
    this.onBookmarkClickListener = listener;
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

  public void setNewUserListModelGroupCallbacks(
      NewUserListModelGroup.NewUserListModelGroupCallbacks newUserListModelGroupCallbacks) {
    this.newUserListModelGroupCallbacks = newUserListModelGroupCallbacks;
  }

  public void addPost(PostRealm post) {
    if (post.isTextQuestionPost()) {
      feedItems.add(getInsertionIndex(), new TextQuestionItem(post));
    } else if (post.isRichQuestionPost()) {
      feedItems.add(getInsertionIndex(), new RichQuestionItem(post));
    } else if (post.isBlogPost()) {
      feedItems.add(getInsertionIndex(), new BlogItem(post));
    }

    setData(feedItems, false);
  }

  public void deletePost(PostRealm post) {
    if (post.isTextQuestionPost()) {
      feedItems.remove(new TextQuestionItem(post));
    } else if (post.isRichQuestionPost()) {
      feedItems.remove(new RichQuestionItem(post));
    } else if (post.isBlogPost()) {
      feedItems.remove(new BlogItem(post));
    }

    setData(feedItems, false);
  }

  public void deleteNewUser(AccountRealm account) {
    boolean removed = false;
    for (FeedItem item : feedItems) {
      if (item instanceof NewUsersFeedItem) {
        removed = ((NewUsersFeedItem) item).getUsers().remove(account);
        break;
      }
    }

    if (removed) {
      setData(feedItems, false);
    }
  }

  public void onRefresh() {
    this.feedItems.clear();
  }

  private void createTextQuestion(PostRealm post) {
    new TextQuestionModel_()
        .id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_text)
        .bitmapTransformation(bitmapTransformation)
        .glide(glide)
        .post(post)
        .addTo(this);
  }

  private void createRichQuestion(PostRealm post) {
    new RichQuestionModel_()
        .id(post.getId())
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
        .bitmapTransformation(bitmapTransformation)
        .glide(glide)
        .set(constraintSet)
        .post(post)
        .addTo(this);
  }

  private void createBlog(PostRealm post) {
    new BlogModel_()
        .id(post.getId())
        .userId(userId)
        .onProfileClickListener(onProfileClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onItemClickListener(onPostClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .bitmapTransformation(bitmapTransformation)
        .glide(glide)
        .post(post)
        .addTo(this);
  }

  private int getInsertionIndex() {
    return feedItems.size() > 3 ? 3 : 2;
  }

  public interface RecommendedGroupListCallbacks {
    void onRecommendedGroupsHeaderClicked();

    void onRecommendedGroupClicked(GroupRealm group);
  }

  public interface TrendingBlogListCallbacks {
    void onTrendingBlogHeaderClicked();

    void onTrendingBlogClicked(PostRealm blog);

    void onTrendingBlogBookmarkClicked(String postId, boolean bookmark);

    void onTrendingBlogOptionsClicked(View v, PostRealm post);
  }
}
