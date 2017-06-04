package com.yoloo.android.feature.postdetail.mvi;

import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.TypedEpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.comment.CommentModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.feature.models.post.PostCallbacks;
import com.yoloo.android.feature.models.post.RichPostModel_;
import com.yoloo.android.feature.models.post.TextPostModel_;

class PostDetailMviEpoxyController extends TypedEpoxyController<PostDetailViewState> {

  private final RequestManager glide;

  @AutoModel LoaderModel loaderModel;

  private PostCallbacks postCallbacks;
  private CommentCallbacks commentCallbacks;

  PostDetailMviEpoxyController(RequestManager glide) {
    this.glide = glide;
  }

  void setPostCallbacks(PostCallbacks postCallbacks) {
    this.postCallbacks = postCallbacks;
  }

  void setCommentCallbacks(CommentCallbacks commentCallbacks) {
    this.commentCallbacks = commentCallbacks;
  }

  void scrollToEnd(RecyclerView recyclerView) {
    recyclerView.smoothScrollToPosition(getAdapter().getItemCount());
  }

  @Override protected void buildModels(PostDetailViewState viewState) {
    Stream.of(viewState.getData()).forEach(item -> {
      if (item instanceof TextPostFeedItem) {
        createTextPostDetail(((TextPostFeedItem) item).getItem());
      } else if (item instanceof RichPostFeedItem) {
        createRichPostDetail(((RichPostFeedItem) item).getItem());
      } else if (item instanceof CommentFeedItem) {
        createCommentModel(((CommentFeedItem) item).getItem());
      }
    });

    loaderModel.addIf(viewState.isLoadingNextPage(), this);
  }

  private void createTextPostDetail(PostRealm post) {
    new TextPostModel_().id(post.getId())
        .post(post)
        .glide(glide)
        .callbacks(postCallbacks)
        .layout(R.layout.item_feed_question_text_detail)
        .detailLayout(true)
        .addTo(this);
  }

  private void createRichPostDetail(PostRealm post) {
    new RichPostModel_().id(post.getId())
        .post(post)
        .glide(glide)
        .callbacks(postCallbacks)
        .layout(R.layout.item_feed_question_rich_detail)
        .detailLayout(true)
        .addTo(this);
  }

  private void createCommentModel(CommentRealm comment) {
    new CommentModel_().id(comment.getId())
        .comment(comment)
        .glide(glide)
        .callbacks(commentCallbacks)
        .addTo(this);
  }
}
