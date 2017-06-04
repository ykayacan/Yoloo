package com.yoloo.android.feature.models.comment;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelClass;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.ui.widget.CommentView;

import static com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash;

@EpoxyModelClass(layout = R.layout.item_comment)
public abstract class CommentModel extends EpoxyModel<CommentView> {

  @EpoxyAttribute CommentRealm comment;
  @EpoxyAttribute(DoNotHash) RequestManager glide;
  @EpoxyAttribute(DoNotHash) CommentCallbacks callbacks;

  @Override
  public void bind(CommentView view) {
    super.bind(view);
    view.setComment(comment);
    view.setUserAvatar(glide, comment.getAvatarUrl());
    view.setCommentCallbacks(callbacks);
  }
}
