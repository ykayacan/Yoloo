package com.yoloo.android.feature.postdetail;

import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.comment.CommentModel;
import com.yoloo.android.feature.comment.CommentModel_;
import com.yoloo.android.feature.comment.OnCommentLongClickListener;
import com.yoloo.android.feature.comment.OnMarkAsAcceptedClickListener;
import com.yoloo.android.feature.feed.common.annotation.PostType;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
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

  private final OnProfileClickListener onProfileClickListener;
  private final OnPostOptionsClickListener onPostOptionsClickListener;
  private final OnShareClickListener onShareClickListener;
  private final OnCommentClickListener onCommentClickListener;
  private final OnContentImageClickListener onContentImageClickListener;
  private final OnVoteClickListener onVoteClickListener;
  private final OnMentionClickListener onMentionClickListener;
  private final OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  private final OnCommentLongClickListener onCommentLongClickListener;

  private final CommentCountModel_ commentCountModel = new CommentCountModel_();

  private PostDetailAdapter(
      OnProfileClickListener onProfileClickListener,
      OnPostOptionsClickListener onPostOptionsClickListener,
      OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      OnContentImageClickListener onContentImageClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener,
      OnCommentLongClickListener onCommentLongClickListener) {
    this.onProfileClickListener = onProfileClickListener;
    this.onPostOptionsClickListener = onPostOptionsClickListener;
    this.onShareClickListener = onShareClickListener;
    this.onCommentClickListener = onCommentClickListener;
    this.onContentImageClickListener = onContentImageClickListener;
    this.onVoteClickListener = onVoteClickListener;
    this.onMentionClickListener = onMentionClickListener;
    this.onMarkAsAcceptedClickListener = onMarkAsAcceptedClickListener;
    this.onCommentLongClickListener = onCommentLongClickListener;

    enableDiffing();
  }

  public static PostDetailAdapterBuilder builder() {
    return new PostDetailAdapterBuilder();
  }

  private static boolean isCommentOwner(CommentRealm comment, String userId) {
    return comment.getOwnerId().equals(userId);
  }

  public void addPost(PostRealm post, boolean refresh) {
    if (!refresh) {
      final int postType = post.getType();

      EpoxyModel<?> model = null;
      if (postType == PostType.TYPE_NORMAL) {
        model = createNormalQuestionDetail(post);
      } else if (postType == PostType.TYPE_RICH) {
        model = createRichQuestionDetail(post);
      } else if (postType == PostType.TYPE_BLOG) {
        model = createBlog(post);
      }

      addModel(model);

      insertModelAfter(commentCountModel, model);
    }
  }

  public void addComments(List<CommentRealm> comments, String userId, boolean postOwner,
      boolean accepted, @PostType int postType) {
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

  public void addAcceptedComment(CommentRealm comment, boolean postOwner, @PostType int postType) {
    addModel(createCommentModel(comment, false, postOwner, true, postType));
  }

  public void addComment(CommentRealm comment, boolean postOwner, @PostType int postType) {
    addModel(createCommentModel(comment, true, postOwner, false, postType));
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

  private RichQuestionModel createRichQuestionDetail(PostRealm post) {
    return new RichQuestionModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
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
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_normal_detail)
        .post(post);
  }

  private BlogModel createBlog(PostRealm post) {
    return new BlogModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog)
        .post(post);
  }

  public static class PostDetailAdapterBuilder {
    private OnProfileClickListener onProfileClickListener;
    private OnPostOptionsClickListener onPostOptionsClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnVoteClickListener onVoteClickListener;
    private OnContentImageClickListener onContentImageClickListener;
    private OnMentionClickListener onMentionClickListener;
    private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
    private OnCommentLongClickListener onCommentLongClickListener;

    PostDetailAdapterBuilder() {
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onProfileClickListener(
        OnProfileClickListener onProfileClickListener) {
      this.onProfileClickListener = onProfileClickListener;
      return this;
    }

    public PostDetailAdapter.PostDetailAdapterBuilder onOptionsClickListener(
        OnPostOptionsClickListener onPostOptionsClickListener) {
      this.onPostOptionsClickListener = onPostOptionsClickListener;
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

    public PostDetailAdapter.PostDetailAdapterBuilder onCommentLongClickListener(
        OnCommentLongClickListener onCommentLongClickListener) {
      this.onCommentLongClickListener = onCommentLongClickListener;
      return this;
    }

    public PostDetailAdapter build() {
      return new PostDetailAdapter(onProfileClickListener, onPostOptionsClickListener,
          onShareClickListener, onCommentClickListener, onContentImageClickListener,
          onVoteClickListener, onMentionClickListener, onMarkAsAcceptedClickListener,
          onCommentLongClickListener);
    }
  }
}