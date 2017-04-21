package com.yoloo.android.feature.postdetail;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.RequestManager;
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
import com.yoloo.android.feature.feed.component.post.BlogModel;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.List;

class PostDetailAdapter extends EpoxyAdapter {

  private final CropCircleTransformation cropCircleTransformation;
  private final RequestManager glide;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnContentImageClickListener onContentImageClickListener;
  private OnVoteClickListener onVoteClickListener;
  private OnMentionClickListener onMentionClickListener;
  private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  private OnItemLongClickListener<CommentRealm> onCommentLongClickListener;

  PostDetailAdapter(Context context, RequestManager glide) {
    enableDiffing();
    cropCircleTransformation = new CropCircleTransformation(context);
    this.glide = glide;
  }

  public void setOnProfileClickListener(OnProfileClickListener listener) {
    this.onProfileClickListener = listener;
  }

  public void setOnPostOptionsClickListener(OnPostOptionsClickListener listener) {
    this.onPostOptionsClickListener = listener;
  }

  public void setOnShareClickListener(OnShareClickListener listener) {
    this.onShareClickListener = listener;
  }

  public void setOnCommentClickListener(OnCommentClickListener listener) {
    this.onCommentClickListener = listener;
  }

  public void setOnContentImageClickListener(OnContentImageClickListener listener) {
    this.onContentImageClickListener = listener;
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

  public void setOnCommentLongClickListener(OnItemLongClickListener<CommentRealm> listener) {
    this.onCommentLongClickListener = listener;
  }

  void addPost(PostRealm post) {
    final int postType = post.getPostType();

    if (postType == PostRealm.TYPE_TEXT) {
      addModel(createNormalQuestionDetail(post));
    } else if (postType == PostRealm.TYPE_RICH) {
      addModel(createRichQuestionDetail(post));
    } else if (postType == PostRealm.TYPE_BLOG) {
      addModel(createBlogDetail(post));
    }

    //addModel(commentHeaderModel);
  }

  void addComments(List<CommentRealm> comments, AccountRealm account, PostRealm post) {
    for (CommentRealm comment : comments) {
      // Don't add accepted comment twice.
      if (comment.isAccepted()) {
        continue;
      }

      models.add(createCommentModel(comment, account, post));
    }

    notifyModelsChanged();
  }

  void addComment(CommentRealm comment, AccountRealm account, PostRealm post) {
    //showCommentsHeader(true);
    addModel(createCommentModel(comment, account, post));
  }

  void clear() {
    removeAllModels();
  }

  void delete(EpoxyModel<?> model) {
    removeModel(model);
  }

  void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getItemCount() - 1);
  }

  private CommentModel createCommentModel(CommentRealm comment, AccountRealm account,
      PostRealm post) {
    return new CommentModel_().glide(glide)
        .comment(comment)
        .isCommentOwner(isCommentOwner(comment, account))
        .isPostOwner(isPostOwner(post, account))
        .postAccepted(isAccepted(post))
        .postType(post.getPostType())
        .backgroundColor(Color.WHITE)
        .circleTransformation(cropCircleTransformation)
        .onCommentLongClickListener(onCommentLongClickListener)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener);
  }

  private RichQuestionModel createRichQuestionDetail(PostRealm post) {
    return new RichQuestionModel_().glide(glide)
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

  private TextQuestionModel createNormalQuestionDetail(PostRealm post) {
    return new TextQuestionModel_().glide(glide)
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_text_detail)
        .circleTransformation(cropCircleTransformation)
        .post(post);
  }

  private BlogModel createBlogDetail(PostRealm post) {
    return new BlogModel_().onProfileClickListener(onProfileClickListener)
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
}
