package com.yoloo.android.feature.postdetail;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.SimpleEpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.comment.CommentModel;
import com.yoloo.android.feature.comment.CommentModel_;
import com.yoloo.android.feature.comment.OnMarkAsAcceptedClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.blogcomponent.BlogModel;
import com.yoloo.android.feature.feed.component.blogcomponent.BlogModel_;
import com.yoloo.android.feature.feed.component.postcomponent.NormalQuestionModel;
import com.yoloo.android.feature.feed.component.postcomponent.NormalQuestionModel_;
import com.yoloo.android.feature.feed.component.postcomponent.RichQuestionModel;
import com.yoloo.android.feature.feed.component.postcomponent.RichQuestionModel_;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
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
  private final OnItemLongClickListener<CommentRealm> onCommentLongClickListener;

  private final SimpleEpoxyModel commentHeaderModel =
      new SimpleEpoxyModel(R.layout.item_comment_header);

  private final CropCircleTransformation cropCircleTransformation;

  private PostDetailAdapter(
      Context context,
      OnProfileClickListener onProfileClickListener,
      OnPostOptionsClickListener onPostOptionsClickListener,
      OnShareClickListener onShareClickListener,
      OnCommentClickListener onCommentClickListener,
      OnContentImageClickListener onContentImageClickListener,
      OnVoteClickListener onVoteClickListener,
      OnMentionClickListener onMentionClickListener,
      OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener,
      OnItemLongClickListener<CommentRealm> onCommentLongClickListener) {
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
    cropCircleTransformation = new CropCircleTransformation(context);
  }

  public static PostDetailAdapterBuilder builder(Context context) {
    return new PostDetailAdapterBuilder(context);
  }

  public void addPost(PostRealm post) {
    final int postType = post.getType();

    if (postType == PostRealm.POST_NORMAL) {
      addModel(createNormalQuestionDetail(post));
    } else if (postType == PostRealm.POST_RICH) {
      addModel(createRichQuestionDetail(post));
    } else if (postType == PostRealm.POST_BLOG) {
      addModel(createBlogDetail(post));
    }

    addModel(commentHeaderModel);
  }

  public void addComments(List<CommentRealm> comments, AccountRealm account, PostRealm post) {
    for (CommentRealm comment : comments) {
      // Don't add accepted comment twice.
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, account, post));
    }

    notifyModelsChanged();
  }

  public void addComment(CommentRealm comment, AccountRealm account, PostRealm post) {
    showCommentsHeader(true);
    addModel(createCommentModel(comment, account, post));
  }

  public void clear() {
    removeAllModels();
  }

  public void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  public void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getItemCount() - 1);
  }

  public void showCommentsHeader(boolean show) {
    commentHeaderModel.show(show);
  }

  private CommentModel createCommentModel(CommentRealm comment, AccountRealm account,
      PostRealm post) {
    return new CommentModel_()
        .comment(comment)
        .isCommentOwner(isCommentOwner(comment, account))
        .isPostOwner(isPostOwner(post, account))
        .postAccepted(isAccepted(post))
        .postType(post.getType())
        .backgroundColor(Color.WHITE)
        .circleTransformation(cropCircleTransformation)
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
        .circleTransformation(cropCircleTransformation)
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
        .circleTransformation(cropCircleTransformation)
        .post(post);
  }

  private BlogModel createBlogDetail(PostRealm post) {
    return new BlogModel_()
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog_detail)
        .circleTransformation(cropCircleTransformation)
        .post(post);
  }

  private boolean isCommentOwner(CommentRealm comment, AccountRealm account) {
    return comment.getOwnerId().equals(account.getId());
  }

  private boolean isPostOwner(PostRealm post, AccountRealm account) {
    return post.getOwnerId().equals(account.getId());
  }

  private boolean isAccepted(PostRealm post) {
    return !TextUtils.isEmpty(post.getAcceptedCommentId());
  }

  public static class PostDetailAdapterBuilder {
    private Context context;
    private OnProfileClickListener onProfileClickListener;
    private OnPostOptionsClickListener onPostOptionsClickListener;
    private OnShareClickListener onShareClickListener;
    private OnCommentClickListener onCommentClickListener;
    private OnVoteClickListener onVoteClickListener;
    private OnContentImageClickListener onContentImageClickListener;
    private OnMentionClickListener onMentionClickListener;
    private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
    private OnItemLongClickListener<CommentRealm> onCommentLongClickListener;

    PostDetailAdapterBuilder(Context context) {
      this.context = context;
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
        OnItemLongClickListener<CommentRealm> onCommentLongClickListener) {
      this.onCommentLongClickListener = onCommentLongClickListener;
      return this;
    }

    public PostDetailAdapter build() {
      return new PostDetailAdapter(context, onProfileClickListener, onPostOptionsClickListener,
          onShareClickListener, onCommentClickListener, onContentImageClickListener,
          onVoteClickListener, onMentionClickListener, onMarkAsAcceptedClickListener,
          onCommentLongClickListener);
    }
  }
}
