package com.yoloo.android.feature.comment;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

public class CommentAdapter extends EpoxyAdapter {

  private final OnItemLongClickListener<CommentRealm> onCommentLongClickListener;
  private final OnProfileClickListener onProfileClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnMentionClickListener onMentionClickListener;
  private final OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  private final int postType;

  private final CropCircleTransformation cropCircleTransformation;

  public CommentAdapter(
      Context context,
      OnItemLongClickListener<CommentRealm> onCommentLongClickListener,
      OnProfileClickListener onProfileClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener,
      int postType) {
    this.onCommentLongClickListener = onCommentLongClickListener;
    this.onProfileClickListener = onProfileClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;
    this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;
    this.postType = postType;

    enableDiffing();
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public void addComments(List<CommentRealm> comments, AccountRealm account, String postOwnerId,
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

  public void addAcceptedComment(CommentRealm comment, AccountRealm account, String postOwnerId,
      boolean acceptedPost) {
    insertModelBefore(createCommentModel(comment, isPostOwner(postOwnerId, account),
        isCommentOwner(comment, account), acceptedPost), models.get(0));
  }

  public void addComment(CommentRealm comment, AccountRealm account, String postOwnerId,
      boolean acceptedPost) {
    addModel(createCommentModel(comment, isPostOwner(postOwnerId, account), true, acceptedPost));
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getItemCount() - 1);
  }

  private CommentModel createCommentModel(CommentRealm comment, boolean isPostOwner,
      boolean isCommentOwner, boolean acceptedPost) {
    return new CommentModel_()
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
