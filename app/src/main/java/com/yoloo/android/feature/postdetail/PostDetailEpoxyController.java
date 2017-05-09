package com.yoloo.android.feature.postdetail;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogFeedItem;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.feature.comment.CommentModel_;
import com.yoloo.android.feature.comment.OnMarkAsAcceptedClickListener;
import com.yoloo.android.feature.feed.common.listener.OnBookmarkClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentClickListener;
import com.yoloo.android.feature.feed.common.listener.OnCommentVoteClickListener;
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
import timber.log.Timber;

class PostDetailEpoxyController extends Typed2EpoxyController<List<FeedItem<?>>, Boolean> {

  private final CropCircleTransformation cropCircleTransformation;
  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  private List<FeedItem<?>> items;

  private OnProfileClickListener onProfileClickListener;
  private OnPostOptionsClickListener onPostOptionsClickListener;
  private OnShareClickListener onShareClickListener;
  private OnCommentClickListener onCommentClickListener;
  private OnContentImageClickListener onContentImageClickListener;
  private OnVoteClickListener onPostVoteClickListener;
  private OnCommentVoteClickListener onCommentVoteClickListener;
  private OnMentionClickListener onMentionClickListener;
  private OnMarkAsAcceptedClickListener onMarkAsAcceptedClickListener;
  private OnItemLongClickListener<CommentRealm> onCommentLongClickListener;
  private OnBookmarkClickListener onBookmarkClickListener;

  PostDetailEpoxyController(Context context, RequestManager glide) {
    this.cropCircleTransformation = new CropCircleTransformation(context);
    this.glide = glide;
    this.items = new ArrayList<>();
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

  public void setOnPostVoteClickListener(OnVoteClickListener listener) {
    this.onPostVoteClickListener = listener;
  }

  public void setOnCommentVoteClickListener(OnCommentVoteClickListener onCommentVoteClickListener) {
    this.onCommentVoteClickListener = onCommentVoteClickListener;
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

  public void setOnBookmarkClickListener(OnBookmarkClickListener onBookmarkClickListener) {
    this.onBookmarkClickListener = onBookmarkClickListener;
  }

  void addComment(CommentRealm comment) {
    items.add(new CommentFeedItem(comment));
    setData(items, false);
  }

  void deleteComment(CommentRealm comment) {
    items.remove(new CommentFeedItem(comment));
    setData(items, false);
  }

  void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getAdapter().getItemCount());
  }

  void showLoader() {
    setData(items, true);
  }

  void hideLoader() {
    setData(items, false);
  }

  public void votePost(PostRealm post, int direction) {
    Timber.d("Post: %s", post);
    FeedItem<PostRealm> item = getPostRealmFeedItem(post);

    ((PostRealm)items.get(0).getItem()).setVoteDir(direction);

    //setData(items, false);
  }

  public void updatePost(PostRealm post) {
    /*FeedItem<PostRealm> item = getPostRealmFeedItem(post);

    int index = items.indexOf(item);
    items.add(index, );

    for (int i = 0; i < items.size(); i++) {
      if (item != null && item.id().equals(items.get(i).id())) {
        (PostRealm)items.get(i).getItem() = item.getItem();
      }
    }*/


  }

  @Nullable private FeedItem<PostRealm> getPostRealmFeedItem(PostRealm post) {
    FeedItem<PostRealm> item = null;
    if (post.isTextPost()) {
      item = new TextPostFeedItem(post);
    } else if (post.isRichPost()) {
      item = new RichPostFeedItem(post);
    } else if (post.isBlogPost()) {
      item = new BlogFeedItem(post);
    }
    return item;
  }

  public void updateComment(CommentRealm comment) {
    /*FeedItem<?> item = new CommentFeedItem(comment);
    final int index = items.indexOf(item);
    items.add(index, item);

    setData(items, false);*/
  }

  @Override
  public void setData(List<FeedItem<?>> items, Boolean loadingMore) {
    this.items = items;
    super.setData(items, loadingMore);
  }

  @Override
  protected void buildModels(List<FeedItem<?>> items, Boolean loadingMore) {
    Stream.of(items).forEach(item -> {
      if (item instanceof TextPostFeedItem) {
        createTextPostDetail(((TextPostFeedItem) item).getItem());
      } else if (item instanceof RichPostFeedItem) {
        createRichPostDetail(((RichPostFeedItem) item).getItem());
      } else if (item instanceof BlogFeedItem) {
        createBlogDetail(((BlogFeedItem) item).getItem());
      } else if (item instanceof CommentFeedItem) {
        createCommentModel(((CommentFeedItem) item).getItem());
      }
    });

    loaderModel.addIf(loadingMore, this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .backgroundColor(Color.WHITE)
        .circleTransformation(cropCircleTransformation)
        .onCommentLongClickListener(onCommentLongClickListener)
        .onProfileClickListener(onProfileClickListener)
        .onMentionClickListener(onMentionClickListener)
        .onMarkAsAcceptedClickListener(onMarkAsAcceptedClickListener)
        .onVoteClickListener(onCommentVoteClickListener)
        .addTo(this);
  }

  private void createRichPostDetail(PostRealm post) {
    new RichQuestionModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onPostVoteClickListener)
        .onContentImageClickListener(onContentImageClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .layout(R.layout.item_feed_question_rich_detail)
        .bitmapTransformation(cropCircleTransformation)
        .addTo(this);
  }

  private void createTextPostDetail(PostRealm post) {
    new TextQuestionModel_()
        .id(post.getId())
        .post(post)
        .glide(glide)
        .onProfileClickListener(onProfileClickListener)
        .onPostOptionsClickListener(onPostOptionsClickListener)
        .onShareClickListener(onShareClickListener)
        .onCommentClickListener(onCommentClickListener)
        .onVoteClickListener(onPostVoteClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
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
        .onVoteClickListener(onPostVoteClickListener)
        .onBookmarkClickListener(onBookmarkClickListener)
        .layout(R.layout.item_feed_blog_detail)
        .bitmapTransformation(cropCircleTransformation)
        .glide(glide)
        .addTo(this);
  }
}
