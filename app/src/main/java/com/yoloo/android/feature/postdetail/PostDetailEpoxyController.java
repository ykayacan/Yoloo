package com.yoloo.android.feature.postdetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.comment.CommentModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.models.post.RichPostModel_;
import com.yoloo.android.feature.models.post.TextPostModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class PostDetailEpoxyController extends Typed2EpoxyController<List<FeedItem<?>>, Boolean> {

  private final CropCircleTransformation cropCircleTransformation;
  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  private List<FeedItem<?>> items;

  private PostCallbacks postCallbacks;
  private CommentCallbacks commentCallbacks;

  PostDetailEpoxyController(Context context, RequestManager glide) {
    this.cropCircleTransformation = new CropCircleTransformation(context);
    this.glide = glide;
    this.items = new ArrayList<>();
  }

  void setPostCallbacks(PostCallbacks postCallbacks) {
    this.postCallbacks = postCallbacks;
  }

  void setCommentCallbacks(CommentCallbacks commentCallbacks) {
    this.commentCallbacks = commentCallbacks;
  }

  void updatePost(@NonNull PostRealm post) {
    if (post.isTextPost()) {
      updateFeedItem(new TextPostFeedItem(post));
    } else if (post.isRichPost()) {
      updateFeedItem(new RichPostFeedItem(post));
    } else if (post.isBlogPost()) {
      updateFeedItem(new BlogPostFeedItem(post));
    }
  }

  void addComment(CommentRealm comment) {
    items.add(new CommentFeedItem(comment));
    setData(items, false);
  }

  void updateComment(@NonNull CommentRealm comment) {
    updateFeedItem(new CommentFeedItem(comment));
  }

  void deleteComment(CommentRealm comment) {
    items.remove(new CommentFeedItem(comment));
    setData(items, false);
  }

  void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getAdapter().getItemCount());
  }

  void showLoader() {
    setData(items, true);
  }

  void hideLoader() {
    setData(items, false);
  }

  public void setLoadMoreData(List<FeedItem<?>> items) {
    this.items.addAll(items);
    setData(items, false);
  }

  @Override
  public void setData(List<FeedItem<?>> items, Boolean loadingMore) {
    this.items = items;
    super.setData(items, loadingMore);
  }

  @Override
  protected void buildModels(List<FeedItem<?>> items, Boolean loadingMore) {
    Stream.of(items).forEach(item -> {
      if (item instanceof TextPostFeedItem) {
        createTextPostDetail(((TextPostFeedItem) item).getItem());
      } else if (item instanceof RichPostFeedItem) {
        createRichPostDetail(((RichPostFeedItem) item).getItem());
      } else if (item instanceof CommentFeedItem) {
        createCommentModel(((CommentFeedItem) item).getItem());
      }
    });

    loaderModel.addIf(loadingMore, this);
  }

  private void createTextPostDetail(PostRealm post) {
    new TextPostModel_()
        .id(post.getId())
        .post(post)
        .groupName(post.getGroupId())
        .glide(glide)
        .transformation(cropCircleTransformation)
        .callbacks(postCallbacks)
        .layout(R.layout.item_feed_question_text_detail)
        .detailLayout(true)
        .addTo(this);
  }

  private void createRichPostDetail(PostRealm post) {
    new RichPostModel_()
        .id(post.getId())
        .post(post)
        .groupName(post.getGroupId())
        .glide(glide)
        .transformation(cropCircleTransformation)
        .callbacks(postCallbacks)
        .layout(R.layout.item_feed_question_rich_detail)
        .detailLayout(true)
        .addTo(this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .showAcceptButton(shouldShowAcceptButton(comment))
        .circleTransformation(cropCircleTransformation)
        .callbacks(commentCallbacks)
        .addTo(this);
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

  private boolean shouldShowAcceptButton(CommentRealm comment) {
    return !comment.isOwner() && comment.isPostOwner() && !comment.isPostAccepted();
  }
}
