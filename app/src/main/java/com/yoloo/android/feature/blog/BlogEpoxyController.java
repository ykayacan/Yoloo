package com.yoloo.android.feature.blog;

import android.content.Context;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogFeedItem;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.feature.blog.models.BlogModel_;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.models.comment.CommentModel_;
import com.yoloo.android.ui.widget.CommentView;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class BlogEpoxyController extends Typed2EpoxyController<List<FeedItem<?>>, Void> {

  private final RequestManager glide;
  private final CropCircleTransformation cropCircleTransformation;

  private OnVoteClickListener onPostVoteClickListener;
  private OnCommentClickListener onCommentClickListener2;
  private OnShareClickListener onShareClickListener;

  private List<FeedItem<?>> items = new ArrayList<>();

  private CommentView.OnCommentClickListener onCommentClickListener;

  BlogEpoxyController(Context context, RequestManager glide) {
    this.glide = glide;
    this.cropCircleTransformation = new CropCircleTransformation(context);
    this.items = new ArrayList<>();
  }

  void setOnCommentClickListener(CommentView.OnCommentClickListener listener) {
    this.onCommentClickListener = listener;
  }

  public void setOnPostVoteClickListener(
      OnVoteClickListener onPostVoteClickListener) {
    this.onPostVoteClickListener = onPostVoteClickListener;
  }

  public void setOnCommentClickListener2(
      OnCommentClickListener onCommentClickListener2) {
    this.onCommentClickListener2 = onCommentClickListener2;
  }

  public void setOnShareClickListener(
      OnShareClickListener onShareClickListener) {
    this.onShareClickListener = onShareClickListener;
  }

  @Override
  public void setData(List<FeedItem<?>> items, Void data2) {
    this.items = items;
    super.setData(items, data2);
  }

  void addBlog(PostRealm blog) {
    items.add(0, new BlogFeedItem(blog));
    setData(items, null);
  }

  void addComments(List<CommentRealm> comments) {
    items.addAll(Stream.of(comments).map(CommentFeedItem::new).toList());
    setData(items, null);
  }

  void addComment(CommentRealm comment) {
    items.add(new CommentFeedItem(comment));
    setData(items, null);
  }

  void removeComment(CommentRealm comment) {
    items.remove(new CommentFeedItem(comment));
    setData(items, null);
  }

  @Override
  protected void buildModels(List<FeedItem<?>> items, Void aVoid) {
    Stream.of(items).forEach(item -> {
      if (item instanceof BlogFeedItem) {
        createBlogModel(((BlogFeedItem) item).getItem());
      } else if (item instanceof CommentFeedItem) {
        createCommentModel(((CommentFeedItem) item).getItem());
      }
    });
  }

  private void createBlogModel(PostRealm post) {
    new BlogModel_().id(post.getId())
        .post(post)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener2)
        .onVoteClickListener(onPostVoteClickListener)
        .addTo(this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .showAcceptButton(false)
        .circleTransformation(cropCircleTransformation)
        .onCommentClickListener(onCommentClickListener)
        .addTo(this);
  }
}
