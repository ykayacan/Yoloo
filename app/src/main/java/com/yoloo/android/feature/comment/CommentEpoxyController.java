package com.yoloo.android.feature.comment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.loader.LoaderModel;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import java.util.ArrayList;
import java.util.List;

import static com.yoloo.android.util.Preconditions.checkNotNull;

class CommentEpoxyController extends Typed2EpoxyController<List<CommentRealm>, Boolean> {

  private final int postType;

  private final RequestManager glide;
  private final CropCircleTransformation cropCircleTransformation;

  @AutoModel LoaderModel loader;

  private List<CommentRealm> items;

  private CommentCallbacks commentCallbacks;

  CommentEpoxyController(Context context, int postType, RequestManager glide) {
    this.postType = postType;
    this.glide = glide;
    this.cropCircleTransformation = new CropCircleTransformation(context);
    this.items = new ArrayList<>();
    setDebugLoggingEnabled(true);
    setData(items, false);
  }

  void setCommentCallbacks(CommentCallbacks commentCallbacks) {
    this.commentCallbacks = commentCallbacks;
  }

  public void setLoadMoreData(List<CommentRealm> items) {
    this.items.addAll(items);
    setData(items, false);
  }

  @Override
  public void setData(List<CommentRealm> items, Boolean loadingMore) {
    this.items = items;
    super.setData(items, checkNotNull(loadingMore, "loadingMore cannot be null."));
  }

  void addComment(CommentRealm comment) {
    items.add(comment);
    setData(items, false);
  }

  void updateComment(CommentRealm comment) {
    final int size = items.size();
    for (int i = 0; i < size; i++) {
      if (items.get(i).getId().equals(comment.getId())) {
        items.set(i, comment);
        break;
      }
    }

    setData(items, false);
  }

  void deleteComment(CommentRealm comment) {
    items.remove(comment);
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

  @Override
  protected void buildModels(List<CommentRealm> comments, Boolean loadingMore) {
    Stream.of(comments).forEach(this::createCommentModel);

    loader.addIf(loadingMore, this);
  }

  private void createCommentModel(CommentRealm comment) {
    new com.yoloo.android.feature.models.comment.CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .showAcceptButton(shouldShowAcceptButton(comment))
        .circleTransformation(cropCircleTransformation)
        .callbacks(commentCallbacks)
        .addTo(this);
  }

  private boolean shouldShowAcceptButton(CommentRealm comment) {
    return !comment.isOwner()
        && comment.isPostOwner()
        && !comment.isPostAccepted()
        && postType != PostRealm.TYPE_BLOG;
  }
}
