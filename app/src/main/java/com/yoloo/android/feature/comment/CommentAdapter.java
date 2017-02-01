package com.yoloo.android.feature.comment;

import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.feed.common.annotation.PostType;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import java.util.List;

public class CommentAdapter extends EpoxyAdapter {

  private final OnCommentLongClickListener onCommentLongClickListener;
  private final OnProfileClickListener onProfileClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnMentionClickListener onMentionClickListener;
  private final OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  @PostType private final int postType;

  public CommentAdapter(
      OnCommentLongClickListener onCommentLongClickListener,
      OnProfileClickListener onProfileClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener,
      @PostType int postType) {
    this.onCommentLongClickListener = onCommentLongClickListener;
    this.onProfileClickListener = onProfileClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;
    this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;
    this.postType = postType;

    enableDiffing();
  }

  private static boolean isCommentOwner(CommentRealm comment, String userId) {
    return comment.getOwnerId().equals(userId);
  }

  public void addComments(List<CommentRealm> comments, String userId, boolean postOwner,
      boolean accepted) {
    for (CommentRealm comment : comments) {
      // Don't add accepted comment twice.
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, isCommentOwner(comment, userId), postOwner, accepted,
          postType));
    }

    notifyModelsChanged();
  }

  public void addAcceptedComment(CommentRealm comment, boolean postOwner) {
    models.add(0, createCommentModel(comment, false, postOwner, true, postType));
    notifyItemInserted(0);
  }

  public void addComment(CommentRealm comment, boolean postOwner) {
    addModel(createCommentModel(comment, true, postOwner, false, postType));
  }

  public void clear() {
    final int size = models.size();
    models.clear();
    notifyItemRangeRemoved(0, size);
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getItemCount() - 1);
  }

  private CommentModel createCommentModel(CommentRealm comment, boolean commentOwner,
      boolean postOwner, boolean postAccepted, @PostType int postType) {
    return new CommentModel_()
        .comment(comment)
        .isCommentOwner(commentOwner)
        .isPostOwner(postOwner)
        .postAccepted(postAccepted)
        .postType(postType)
        .onCommentLongClickListener(onCommentLongClickListener)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener);
  }
}
