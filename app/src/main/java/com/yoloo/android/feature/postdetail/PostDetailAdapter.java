package com.yoloo.android.feature.postdetail;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.comment.CommentModel_;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel_;
import com.yoloo.android.feature.postdetail.model.CommentCountModel_;
import java.util.List;

public class PostDetailAdapter extends EpoxyAdapter {

  private final OnProfileClickListener onProfileClickListener;
  private final OnOptionsClickListener onOptionsClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnContentImageClickListener onContentImageClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnMentionClickListener onMentionClickListener;

  private final CommentCountModel_ commentCountModel = new CommentCountModel_();

  private PostDetailAdapter(
      OnProfileClickListener onProfileClickListener,
      OnOptionsClickListener onOptionsClickListener,
      OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      OnContentImageClickListener onContentImageClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onOptionsClickListener = onOptionsClickListener;
    this.onShareClickListener = onShareClickListener;
    this.onCommentClickListener = onCommentClickListener;
    this.onContentImageClickListener = onContentImageClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;

    enableDiffing();
  }

  public static PostDetailAdapterBuilder builder() {
    return new PostDetailAdapterBuilder();
  }

  public void addQuestion(PostRealm post) {
    NormalQuestionModel_ normalQuestionModel_ = new NormalQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onOptionsClickListener(onOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_question_normal_detail)
        .post(post);

    addModel(normalQuestionModel_);

    commentCountModel.counts(post.getComments());
    insertModelAfter(commentCountModel, normalQuestionModel_);
  }

  public void addComments(List<CommentRealm> comments) {
    for (CommentRealm comment : comments) {
      models.add(new CommentModel_()
          .comment(comment)
          .onProfileClickListener(onProfileClickListener)
          .onMentionClickListener(onMentionClickListener)
          .onVoteClickListener(onVoteClickListener));
    }

    notifyModelsChanged();
  }

  public void addComment(CommentRealm comment, boolean accepted) {
    CommentModel_ model_ = new CommentModel_()
        .comment(comment)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onVoteClickListener(onVoteClickListener);

    if (accepted) {
      insertModelAfter(model_, commentCountModel);
    } else {
      addModel(model_);
    }
  }

  public void clear() {
    removeAllAfterModel(commentCountModel);
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public static class PostDetailAdapterBuilder {
    private OnProfileClickListener onProfileClickListener;
    private OnOptionsClickListener onOptionsClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnVoteClickListener onVoteClickListener;
    private OnContentImageClickListener onContentImageClickListener;
    private OnMentionClickListener onMentionClickListener;

    PostDetailAdapterBuilder() {
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onProfileClickListener(
        OnProfileClickListener onProfileClickListener) {
      this.onProfileClickListener = onProfileClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onOptionsClickListener(
        OnOptionsClickListener onOptionsClickListener) {
      this.onOptionsClickListener = onOptionsClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onShareClickListener(
        OnShareClickListener onShareClickListener) {
      this.onShareClickListener = onShareClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onCommentClickListener(
        OnCommentClickListener onCommentClickListener) {
      this.onCommentClickListener = onCommentClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onVoteClickListener(
        OnVoteClickListener onVoteClickListener) {
      this.onVoteClickListener = onVoteClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onContentImageClickListener(
        OnContentImageClickListener onContentImageClickListener) {
      this.onContentImageClickListener = onContentImageClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onMentionClickListener(
        OnMentionClickListener onMentionClickListener) {
      this.onMentionClickListener = onMentionClickListener;
      return this;
    }

    public PostDetailAdapter build() {
      return new PostDetailAdapter(onProfileClickListener, onOptionsClickListener,
          onShareClickListener, onCommentClickListener, onContentImageClickListener,
          onVoteClickListener, onMentionClickListener);
    }
  }
}