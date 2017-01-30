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
  @PostType private final int postType;

  public CommentAdapter(
      OnProfileClickListener onProfileClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener,
      @PostType int postType) {
    this.onProfileClickListener = onProfileClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;
    this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;
    this.postType = postType;

    enableDiffing();
  }

  public void addComments(List<CommentRealm> comments, boolean self, boolean hasAcceptedId) {
    for (CommentRealm comment : comments) {
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, self, hasAcceptedId, postType));
    }

    notifyModelsChanged();
  }

  public void addAcceptedComment(CommentRealm comment, boolean self, boolean hasAcceptedId) {
    models.add(0, createCommentModel(comment, self, hasAcceptedId, postType));
    notifyItemInserted(0);
  }

  public void addComment(CommentRealm comment, boolean self, boolean hasAcceptedId) {
    addModel(createCommentModel(comment, self, hasAcceptedId, postType));
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
      boolean hasAcceptedId, @PostType int postType) {
    return new CommentModel_().comment(comment)
        .self(self)
        .hasAcceptedId(hasAcceptedId)
        .postType(postType)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener);
  }
}
