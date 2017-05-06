package com.yoloo.android.feature.postdetail;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.FeedItem;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.feature.comment.CommentModel_;
import com.yoloo.android.feature.comment.OnMarkAsAcceptedClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnContentImageClickListener;
import com.yoloo.android.feature.feed.common.listener.OnMentionClickListener;
import com.yoloo.android.feature.feed.common.listener.OnPostOptionsClickListener;
import com.yoloo.android.feature.feed.common.listener.OnProfileClickListener;
import com.yoloo.android.feature.feed.common.listener.OnShareClickListener;
import com.yoloo.android.feature.feed.common.listener.OnVoteClickListener;
import com.yoloo.android.feature.feed.component.post.BlogModel_;
import com.yoloo.android.feature.feed.component.post.RichQuestionModel_;
import com.yoloo.android.feature.feed.component.post.TextQuestionModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.ui.recyclerview.OnItemLongClickListener;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

class PostDetailEpoxyController extends Typed2EpoxyController<List<FeedItem>, Boolean> {

  private final CropCircleTransformation cropCircleTransformation;
  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  private List<FeedItem> epoxyItems;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnContentImageClickListener onContentImageClickListener;
  private OnVoteClickListener onVoteClickListener;
  private OnMentionClickListener onMentionClickListener;
  private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  private OnItemLongClickListener<CommentRealm> onCommentLongClickListener;

  PostDetailEpoxyController(Context context, RequestManager glide) {
    this.cropCircleTransformation = new CropCircleTransformation(context);
    this.glide = glide;
    this.epoxyItems = new ArrayList<>();
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

  void addComment(CommentRealm comment) {
    epoxyItems.add(new CommentFeedItem(comment));
    setData(epoxyItems, false);
  }

  void deleteComment(CommentRealm comment) {
    epoxyItems.remove(new CommentFeedItem(comment));
    setData(epoxyItems, false);
  }

  void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getAdapter().getItemCount() - 1);
  }

  @Override
  public void setData(List<FeedItem> items, Boolean loadingMore) {
    this.epoxyItems = items;
    super.setData(items, loadingMore);
  }

  @Override
  protected void buildModels(List<FeedItem> items, Boolean loadingMore) {
    Stream.of(items).forEach(item -> {
      if (item instanceof PostFeedItem) {
        PostRealm post = ((PostFeedItem) item).getPost();
        if (post.isTextQuestionPost()) {
          createTextQuestionDetail(post);
        } else if (post.isRichQuestionPost()) {
          createRichQuestionDetail(post);
        } else if (post.isBlogPost()) {
          createBlogDetail(post);
        }
      } else if (item instanceof CommentFeedItem) {
        createCommentModel(((CommentFeedItem) item).getComment());
      }
    });

    loaderModel.addIf(loadingMore, this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .postType(comment.getPostType())
        .backgroundColor(Color.WHITE)
        .circleTransformation(cropCircleTransformation)
        .onCommentLongClickListener(onCommentLongClickListener)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onVoteClickListener)
        .addTo(this);
  }

  private void createRichQuestionDetail(PostRealm post) {
    new RichQuestionModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .onContentImageClickListener(onContentImageClickListener)
        .layout(R.layout.item_feed_question_rich_detail)
        .bitmapTransformation(cropCircleTransformation)
        .addTo(this);
  }

  private void createTextQuestionDetail(PostRealm post) {
    new TextQuestionModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_question_text_detail)
        .bitmapTransformation(cropCircleTransformation)
        .addTo(this);
  }

  private void createBlogDetail(PostRealm post) {
    new BlogModel_()
        .id(post.getId())
        .post(post)
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onVoteClickListener)
        .layout(R.layout.item_feed_blog_detail)
        .bitmapTransformation(cropCircleTransformation)
        .glide(glide)
        .addTo(this);
  }
}
