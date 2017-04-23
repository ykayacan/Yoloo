package com.yoloo.android.feature.blog;

import android.content.Context;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.ui.widget.CommentView;
import com.yoloo.android.feature.blog.models.BlogModel_;
import com.yoloo.android.feature.blog.models.CommentModel_;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.Collections;
import java.util.List;

public class BlogAdapter extends EpoxyAdapter {

  private final RequestManager glide;
  private final CropCircleTransformation cropCircleTransformation;

  private CommentView.OnCommentClickListener onCommentClickListener;

  public BlogAdapter(Context context, RequestManager glide) {
    this.glide = glide;
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public void setOnCommentClickListener(CommentView.OnCommentClickListener listener) {
    this.onCommentClickListener = listener;
  }

  public void addBlog(PostRealm post) {
    addModel(createBlogModel(post));
  }

  public void addComments(List<CommentRealm> comments) {
    for (CommentRealm comment : comments) {
      addModel(createCommentModel(comment));
    }
  }

  public void addComment(CommentRealm comment) {
    addComments(Collections.singletonList(comment));
  }

  public void deleteComment(EpoxyModel<?> model) {
    removeModel(model);
  }

  private BlogModel_ createBlogModel(PostRealm post) {
    return new BlogModel_().id(post.getId()).post(post);
  }

  private CommentModel_ createCommentModel(CommentRealm comment) {
    return new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .acceptable(false)
        .circleTransformation(cropCircleTransformation)
        .onCommentClickListener(onCommentClickListener);
  }
}
