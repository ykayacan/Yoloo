package com.yoloo.android.feature.comment;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class CommentAdapter extends EpoxyAdapter {

  private final int postType;

  private final RequestManager glide;

  private final CropCircleTransformation cropCircleTransformation;

  private OnItemLongClickListener<CommentRealm> onCommentLongClickListener;
  private OnProfileClickListener onProfileClickListener;
  private OnVoteClickListener onVoteClickListener;
  private OnMentionClickListener onMentionClickListener;
  private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;

  CommentAdapter(Context context, int postType, RequestManager glide) {
    this.postType = postType;
    this.glide = glide;

    enableDiffing();
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public void setOnCommentLongClickListener(OnItemLongClickListener<CommentRealm> listener) {
    this.onCommentLongClickListener = listener;
  }

  public void setOnProfileClickListener(OnProfileClickListener listener) {
    this.onProfileClickListener = listener;
  }

  public void setOnVoteClickListener(OnVoteClickListener listener) {
    this.onVoteClickListener = listener;
  }

  public void setOnMentionClickListener(OnMentionClickListener listener) {
    this.onMentionClickListener = listener;
  }

  public void setOnMarkAsAcceptedClickListener(OnMarkAsAcceptedClickListener listener) {
    this.onMarkAsAcceptedClickListener = listener;
  }

  void addComments(List<CommentRealm> comments, AccountRealm account, String postOwnerId,
      boolean acceptedPost) {
    for (CommentRealm comment : comments) {
      // Don't addPost accepted comment twice.
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, isPostOwner(postOwnerId, account),
          isCommentOwner(comment, account), acceptedPost));
    }

    notifyModelsChanged();
  }

  void addAcceptedComment(CommentRealm comment, AccountRealm account, String postOwnerId,
      boolean acceptedPost) {
    insertModelBefore(createCommentModel(comment, isPostOwner(postOwnerId, account),
        isCommentOwner(comment, account), acceptedPost), models.get(0));
  }

  void addComment(CommentRealm comment, AccountRealm account, String postOwnerId,
      boolean acceptedPost) {
    addModel(createCommentModel(comment, isPostOwner(postOwnerId, account), true, acceptedPost));
  }

  void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getItemCount() - 1);
  }

  private CommentModel createCommentModel(CommentRealm comment, boolean isPostOwner,
      boolean isCommentOwner, boolean acceptedPost) {
    return new CommentModel_().glide(glide)
        .comment(comment)
        .isCommentOwner(isCommentOwner)
        .isPostOwner(isPostOwner)
        .postAccepted(acceptedPost)
        .postType(postType)
        .backgroundColor(Color.TRANSPARENT)
        .circleTransformation(cropCircleTransformation)
        .onCommentLongClickListener(onCommentLongClickListener)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener);
  }

  private boolean isCommentOwner(CommentRealm comment, AccountRealm account) {
    return comment.getOwnerId().equals(account.getId());
  }

  private boolean isPostOwner(String postOwnerId, AccountRealm account) {
    return postOwnerId.equals(account.getId());
  }
}
