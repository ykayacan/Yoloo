package com.yoloo.android.feature.comment;

import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import java.util.List;

public class CommentAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnMentionClickListener onMentionClickListener;
  private final OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;

  public CommentAdapter(
      OnProfileClickListener onProfileClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;
    this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;

    enableDiffing();
  }

  public void addComments(List<CommentRealm> comments, boolean self, boolean hasAcceptedId) {
    for (CommentRealm comment : comments) {
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, self, hasAcceptedId));
    }

    notifyModelsChanged();
  }

  public void addAcceptedComment(CommentRealm comment, boolean self, boolean hasAcceptedId) {
    models.add(0, createCommentModel(comment, self, hasAcceptedId));
    notifyItemInserted(0);
  }

  public void addComment(CommentRealm comment, boolean self, boolean hasAcceptedId) {
    addModel(createCommentModel(comment, self, hasAcceptedId));
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

  private CommentModel createCommentModel(CommentRealm comment, boolean self,
      boolean hasAcceptedId) {
    return new CommentModel_().comment(comment)
        .self(self)
        .hasAcceptedId(hasAcceptedId)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener);
  }
}
