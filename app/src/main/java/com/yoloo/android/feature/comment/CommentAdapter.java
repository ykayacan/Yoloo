package com.yoloo.android.feature.comment;

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

  public CommentAdapter(OnProfileClickListener onProfileClickListener,
      OnVoteClickListener onVoteClickListener, OnMentionClickListener onMentionClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;

    enableDiffing();
  }

  public void addComments(List<CommentRealm> comments) {
    for (CommentRealm comment : comments) {
      models.add(new CommentModel_().comment(comment)
          .onProfileClickListener(onProfileClickListener)
          .onMentionClickListener(onMentionClickListener)
          .onVoteClickListener(onVoteClickListener));
    }

    notifyModelsChanged();
  }

  public void addComment(CommentRealm comment, boolean accepted) {
    CommentModel_ model_ = new CommentModel_().comment(comment)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onVoteClickListener(onVoteClickListener);

    if (accepted) {
      insertModelBefore(model_, models.get(0));
    } else {
      addModel(model_);
    }
  }

  public void clear() {
    final int size = models.size();
    models.clear();
    notifyItemRangeRemoved(0, size);
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }
}
