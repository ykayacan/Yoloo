package com.yoloo.android.feature.blog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.feature.models.BlogModel_;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.comment.CommentModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class BlogEpoxyController extends Typed2EpoxyController<List<FeedItem<?>>, Boolean> {

  private final CropCircleTransformation cropCircleTransformation;
  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  private List<FeedItem<?>> items;

  private String userId;

  private PostCallbacks postCallbacks;
  private CommentCallbacks commentCallbacks;

  BlogEpoxyController(Context context, RequestManager glide) {
    this.glide = glide;
    this.cropCircleTransformation = new CropCircleTransformation(context);
    this.items = new ArrayList<>();
  }

  void setUserId(String userId) {
    this.userId = userId;
  }

  void setPostCallbacks(PostCallbacks postCallbacks) {
    this.postCallbacks = postCallbacks;
  }

  void setCommentCallbacks(CommentCallbacks commentCallbacks) {
    this.commentCallbacks = commentCallbacks;
  }

  @Override
  public void setData(List<FeedItem<?>> items, Boolean loadingMore) {
    this.items = items;
    super.setData(items, loadingMore);
  }

  void addBlog(PostRealm blog) {
    items.add(0, new BlogPostFeedItem(blog));
    setData(items, false);
  }

  void updateBlog(@NonNull PostRealm post) {
    updateFeedItem(new BlogPostFeedItem(post));
  }

  void addComments(List<CommentRealm> comments) {
    items.addAll(Stream.of(comments).map(CommentFeedItem::new).toList());
    setData(items, false);
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

  public void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getAdapter().getItemCount());
  }

  public void showLoader() {
    setData(items, true);
  }

  public void hideLoader() {
    setData(items, false);
  }

  @Override
  protected void buildModels(List<FeedItem<?>> items, Boolean loadingMore) {
    Stream.of(items).forEach(item -> {
      if (item instanceof BlogPostFeedItem) {
        createBlogModel(((BlogPostFeedItem) item).getItem());
      } else if (item instanceof CommentFeedItem) {
        createCommentModel(((CommentFeedItem) item).getItem());
      }
    });
  }

  private void createBlogModel(PostRealm post) {
    new BlogModel_().id(post.getId())
        .post(post)
        .postOwner(post.getOwnerId().equals(userId))
        .callbacks(postCallbacks)
        .addTo(this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .showAcceptButton(false)
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
}
