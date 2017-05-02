package com.yoloo.android.feature.blog;

import android.content.Context;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.blog.data.BlogItem;
import com.yoloo.android.feature.blog.data.CommentItem;
import com.yoloo.android.feature.blog.models.BlogModel_;
import com.yoloo.android.feature.blog.models.CommentModel_;
import com.yoloo.android.ui.widget.CommentView;
import com.yoloo.android.util.EpoxyItem;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class BlogEpoxyController extends Typed2EpoxyController<List<EpoxyItem<?>>, Void> {

  private final RequestManager glide;
  private final CropCircleTransformation cropCircleTransformation;

  private List<EpoxyItem<?>> items = new ArrayList<>();

  private CommentView.OnCommentClickListener onCommentClickListener;

  BlogEpoxyController(Context context, RequestManager glide) {
    this.glide = glide;
    this.cropCircleTransformation = new CropCircleTransformation(context);
  }

  void setOnCommentClickListener(CommentView.OnCommentClickListener listener) {
    this.onCommentClickListener = listener;
  }

  private void createBlogModel(PostRealm post) {
    new BlogModel_().id(post.getId()).post(post).addTo(this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .acceptable(false)
        .circleTransformation(cropCircleTransformation)
        .onCommentClickListener(onCommentClickListener)
        .addTo(this);
  }

  @Override
  public void setData(List<EpoxyItem<?>> items, Void data2) {
    this.items = items;
    super.setData(items, data2);
  }

  void addBlog(PostRealm blog) {
    items.add(0, new BlogItem(blog));
    setData(items, null);
  }

  void addComments(List<CommentRealm> comments) {
    items.addAll(Stream.of(comments).map(CommentItem::new).toList());
    setData(items, null);
  }

  void addComment(CommentRealm comment) {
    items.add(items.size() - 1, new CommentItem(comment));
    setData(items, null);
  }

  void removeComment(CommentRealm comment) {
    items.remove(new CommentItem(comment));
    setData(items, null);
  }

  @Override
  protected void buildModels(List<EpoxyItem<?>> items, Void aVoid) {
    Stream.of(items).forEach(item -> {
      if (item instanceof BlogItem) {
        createBlogModel(((BlogItem) item).getItem());
      } else if (item instanceof CommentItem) {
        createCommentModel(((CommentItem) item).getItem());
      }
    });
  }
}
