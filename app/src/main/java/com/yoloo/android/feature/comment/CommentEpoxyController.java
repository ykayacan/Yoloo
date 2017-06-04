package com.yoloo.android.feature.comment;

import android.support.v7.widget.RecyclerView;
import com.airbnb.epoxy.AutoModel;
import com.airbnb.epoxy.Typed2EpoxyController;
import com.annimon.stream.Stream;
import com.bumptech.glide.RequestManager;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.feature.models.comment.CommentCallbacks;
import com.yoloo.android.feature.models.comment.CommentModel_;
import com.yoloo.android.feature.models.loader.LoaderModel;
import java.util.ArrayList;
import java.util.List;

import static com.yoloo.android.util.Preconditions.checkNotNull;

class CommentEpoxyController extends Typed2EpoxyController<List<CommentRealm>, Boolean> {

  private final RequestManager glide;

  @AutoModel LoaderModel loader;

  private List<CommentRealm> items;

  private CommentCallbacks commentCallbacks;

  CommentEpoxyController(RequestManager glide) {
    this.glide = glide;
    this.items = new ArrayList<>();
    setDebugLoggingEnabled(true);
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
    new CommentModel_()
        .id(comment.getId())
        .comment(comment)
        .glide(glide)
        .callbacks(commentCallbacks)
        .addTo(this);
  }
}
