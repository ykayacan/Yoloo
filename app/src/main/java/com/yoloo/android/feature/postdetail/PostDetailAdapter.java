package com.yoloo.android.feature.postdetail;

import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.comment.CommentModel;
import com.yoloo.android.feature.comment.CommentModel_;
import com.yoloo.android.feature.comment.OnMarkAsAcceptedClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.common.model.BlogModel;
import com.yoloo.android.feature.feed.common.model.BlogModel_;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel;
import com.yoloo.android.feature.feed.common.model.NormalQuestionModel_;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel;
import com.yoloo.android.feature.feed.common.model.RichQuestionModel_;
import com.yoloo.android.feature.postdetail.model.CommentCountModel_;
import java.util.List;

public class PostDetailAdapter extends EpoxyAdapter {

  public static final int TYPE_NORMAL_QUESTION = 0;
  public static final int TYPE_RICH_QUESTION = 1;
  public static final int TYPE_BLOG = 2;

  private final OnProfileClickListener onProfileClickListener;
  private final OnOptionsClickListener onOptionsClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnContentImageClickListener onContentImageClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnMentionClickListener onMentionClickListener;
  private final OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;

  private final CommentCountModel_ commentCountModel = new CommentCountModel_();

  private PostDetailAdapter(
      OnProfileClickListener onProfileClickListener,
      OnOptionsClickListener onOptionsClickListener,
      OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      OnContentImageClickListener onContentImageClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onOptionsClickListener = onOptionsClickListener;
    this.onShareClickListener = onShareClickListener;
    this.onCommentClickListener = onCommentClickListener;
    this.onContentImageClickListener = onContentImageClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;
    this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;

    enableDiffing();
  }

  public static PostDetailAdapterBuilder builder() {
    return new PostDetailAdapterBuilder();
  }

  public void addPost(PostRealm post) {
    final int postType = post.getType();

    EpoxyModel<?> model = null;
    if (postType == TYPE_NORMAL_QUESTION) {
      model = createNormalQuestionDetail(post);
    } else if (postType == TYPE_RICH_QUESTION) {
      model = createRichQuestionDetail(post);
    } else if (postType == TYPE_BLOG) {
      model = createBlog(post);
    }

    addModel(model);

    commentCountModel.counts(post.getComments());
    insertModelAfter(commentCountModel, model);
  }

  public void addAcceptedComment(CommentRealm comment) {
    insertModelAfter(createCommentModel(comment, false), commentCountModel);
  }

  public void addComment(CommentRealm comment) {
    addModel(createCommentModel(comment, true));
  }

  public void addComments(List<CommentRealm> comments, boolean self) {
    for (CommentRealm comment : comments) {
      // Don't add accepted comment twice.
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, self));
    }

    notifyModelsChanged();
  }

  public void clear() {
    removeAllAfterModel(commentCountModel);
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getItemCount() - 1);
  }

  private CommentModel createCommentModel(CommentRealm comment, boolean self) {
    return new CommentModel_().comment(comment)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener);
  }

  private RichQuestionModel createRichQuestionDetail(PostRealm post) {
    return new RichQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onOptionsClickListener(onOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onContentImageClickListener(onContentImageClickListener)
        .layout(R.layout.item_feed_question_rich_detail)
        .post(post);
  }

  private NormalQuestionModel createNormalQuestionDetail(PostRealm post) {
    return new NormalQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onOptionsClickListener(onOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_normal_detail)
        .post(post);
  }

  private BlogModel createBlog(PostRealm post) {
    return new BlogModel_()
        .onProfileClickListener(onProfileClickListener)
        .onOptionsClickListener(onOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .post(post);
  }

  public static class PostDetailAdapterBuilder {
    private OnProfileClickListener onProfileClickListener;
    private OnOptionsClickListener onOptionsClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnVoteClickListener onVoteClickListener;
    private OnContentImageClickListener onContentImageClickListener;
    private OnMentionClickListener onMentionClickListener;
    private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;

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

    public PostDetailAdapter.PostDetailAdapterBuilder onMarkAsAcceptedListener(
        OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener) {
      this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;
      return this;
    }

    public PostDetailAdapter build() {
      return new PostDetailAdapter(onProfileClickListener, onOptionsClickListener,
          onShareClickListener, onCommentClickListener, onContentImageClickListener,
          onVoteClickListener, onMentionClickListener, onMarkAsAcceptedClickListener);
    }
  }
}