package com.yoloo.android.feature.blog.models;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.ui.widget.CommentView;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

@EpoxyModelClass(layout = R.layout.item_comment2)
public abstract class CommentModel extends EpoxyModel<CommentView> {
  @EpoxyAttribute CommentRealm comment;
  @EpoxyAttribute boolean acceptable;
  @EpoxyAttribute(hash = false) RequestManager glide;
  @EpoxyAttribute(hash = false) CropCircleTransformation circleTransformation;
  @EpoxyAttribute(hash = false) CommentView.OnCommentClickListener onCommentClickListener;

  @Override
  public void bind(CommentView view) {
    super.bind(view);
    view.setUserAvatar(glide, circleTransformation, comment.getAvatarUrl());
    view.setUsername(comment.getUsername());
    view.setTime(comment.getCreated());
    view.setContent(comment.getContent());
    view.setVoteCount(comment.getVoteCount());
    view.setVoteDirection(comment.getVoteDir());
    view.setAcceptedMarkIndicatorVisibility(comment.isAccepted());
    view.showAccept(acceptable && !comment.isAccepted() && comment.isPostOwner());
    view.setOnCommentClickListener(onCommentClickListener);
  }
}
