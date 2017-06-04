package com.yoloo.android.feature.postdetail.mvi;

import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.util.Objects;
import java.util.Collections;
import java.util.List;

final class PostDetailViewState {

  private final boolean loadingFirstPage;
  private final Throwable firstPageError;

  private final List<FeedItem<?>> data;

  private final boolean loadingNextPage;
  private final Throwable nextPageError;

  private final boolean loadingPullToRefresh;
  private final Throwable pullToRefreshError;

  private final boolean newComment;
  private final Throwable newCommentError;

  private final Throwable bookmarkError;

  private final boolean deleteComment;
  private final Throwable deleteCommentError;

  private final boolean deletePost;
  private final Throwable deletePostError;

  private final boolean votePost;
  private final Throwable votePostError;

  private final boolean voteComment;
  private final Throwable voteCommentError;

  private final boolean acceptComment;
  private final Throwable acceptCommentError;

  private PostDetailViewState(List<FeedItem<?>> data, boolean loadingFirstPage,
      Throwable firstPageError, boolean loadingNextPage, Throwable nextPageError,
      boolean loadingPullToRefresh, Throwable pullToRefreshError, boolean newComment,
      Throwable newCommentError, Throwable bookmarkError, boolean deleteComment,
      Throwable deleteCommentError, boolean deletePost, Throwable deletePostError, boolean votePost,
      Throwable votePostError, boolean voteComment, Throwable voteCommentError,
      boolean acceptComment, Throwable acceptCommentError) {
    this.data = data;
    this.loadingNextPage = loadingNextPage;
    this.nextPageError = nextPageError;
    this.loadingPullToRefresh = loadingPullToRefresh;
    this.pullToRefreshError = pullToRefreshError;
    this.firstPageError = firstPageError;
    this.loadingFirstPage = loadingFirstPage;
    this.newComment = newComment;
    this.newCommentError = newCommentError;
    this.bookmarkError = bookmarkError;
    this.deleteComment = deleteComment;
    this.deleteCommentError = deleteCommentError;
    this.deletePost = deletePost;
    this.deletePostError = deletePostError;
    this.votePost = votePost;
    this.votePostError = votePostError;
    this.voteComment = voteComment;
    this.voteCommentError = voteCommentError;
    this.acceptComment = acceptComment;
    this.acceptCommentError = acceptCommentError;
  }

  boolean isLoadingFirstPage() {
    return loadingFirstPage;
  }

  Throwable getFirstPageError() {
    return firstPageError;
  }

  List<FeedItem<?>> getData() {
    return data;
  }

  boolean isLoadingNextPage() {
    return loadingNextPage;
  }

  Throwable getNextPageError() {
    return nextPageError;
  }

  boolean isLoadingPullToRefresh() {
    return loadingPullToRefresh;
  }

  Throwable getPullToRefreshError() {
    return pullToRefreshError;
  }

  boolean isNewComment() {
    return newComment;
  }

  Throwable getNewCommentError() {
    return newCommentError;
  }

  Throwable getBookmarkError() {
    return bookmarkError;
  }

  boolean isDeleteComment() {
    return deleteComment;
  }

  Throwable getDeleteCommentError() {
    return deleteCommentError;
  }

  boolean isDeletePost() {
    return deletePost;
  }

  Throwable getDeletePostError() {
    return deletePostError;
  }

  boolean isVotePost() {
    return votePost;
  }

  Throwable getVotePostError() {
    return votePostError;
  }

  boolean isVoteComment() {
    return voteComment;
  }

  Throwable getVoteCommentError() {
    return voteCommentError;
  }

  boolean isAcceptComment() {
    return acceptComment;
  }

  Throwable getAcceptCommentError() {
    return acceptCommentError;
  }

  Builder builder() {
    return new Builder(this);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PostDetailViewState that = (PostDetailViewState) o;
    return loadingFirstPage == that.loadingFirstPage
        && loadingNextPage == that.loadingNextPage
        && loadingPullToRefresh == that.loadingPullToRefresh
        && newComment == that.newComment
        && deleteComment == that.deleteComment
        && deletePost == that.deletePost
        && votePost == that.votePost
        && voteComment == that.voteComment
        && Objects.equal(firstPageError, that.firstPageError)
        && Objects.equal(data, that.data)
        && Objects.equal(nextPageError, that.nextPageError)
        && Objects.equal(pullToRefreshError, that.pullToRefreshError)
        && Objects.equal(newCommentError, that.newCommentError)
        && Objects.equal(bookmarkError, that.bookmarkError)
        && Objects.equal(deleteCommentError, that.deleteCommentError)
        && Objects.equal(deletePostError, that.deletePostError)
        && Objects.equal(votePostError, that.votePostError)
        && Objects.equal(voteCommentError, that.voteCommentError)
        && Objects.equal(acceptComment, that.acceptComment)
        && Objects.equal(acceptCommentError, that.acceptCommentError);
  }

  @Override public int hashCode() {
    return Objects.hashCode(loadingFirstPage, firstPageError, data, loadingNextPage, nextPageError,
        loadingPullToRefresh, pullToRefreshError, newComment, newCommentError, bookmarkError,
        deleteComment, deleteCommentError, deletePost, deletePostError, votePost, votePostError,
        voteComment, voteCommentError, acceptComment, acceptCommentError);
  }

  @Override public String toString() {
    return "PostDetailViewState{"
        + "loadingFirstPage="
        + loadingFirstPage
        + ", firstPageError="
        + firstPageError
        + ", data="
        + data
        + ", loadingNextPage="
        + loadingNextPage
        + ", nextPageError="
        + nextPageError
        + ", loadingPullToRefresh="
        + loadingPullToRefresh
        + ", pullToRefreshError="
        + pullToRefreshError
        + ", newComment="
        + newComment
        + ", newCommentError="
        + newCommentError
        + ", bookmarkError="
        + bookmarkError
        + ", deleteComment="
        + deleteComment
        + ", deleteCommentError="
        + deleteCommentError
        + ", deletePost="
        + deletePost
        + ", deletePostError="
        + deletePostError
        + ", votePost="
        + votePost
        + ", votePostError="
        + votePostError
        + ", voteComment="
        + voteComment
        + ", voteCommentError="
        + voteCommentError
        + ", acceptComment="
        + acceptComment
        + ", acceptCommentError="
        + acceptCommentError
        + '}';
  }

  static final class Builder {
    private boolean loadingFirstPage;
    private Throwable firstPageError;

    private List<FeedItem<?>> data;

    private boolean loadingNextPage;
    private Throwable nextPageError;

    private boolean loadingPullToRefresh;
    private Throwable pullToRefreshError;

    private boolean newComment;
    private Throwable newCommentError;

    private Throwable bookmarkError;

    private boolean deleteComment;
    private Throwable deleteCommentError;

    private boolean deletePost;
    private Throwable deletePostError;

    private boolean votePost;
    private Throwable votePostError;

    private boolean voteComment;
    private Throwable voteCommentError;

    boolean acceptComment;
    Throwable acceptCommentError;

    Builder() {
      data = Collections.emptyList();
    }

    Builder(PostDetailViewState toCopyFrom) {
      this.loadingFirstPage = toCopyFrom.isLoadingFirstPage();
      this.firstPageError = toCopyFrom.getFirstPageError();

      this.data = toCopyFrom.getData();

      this.loadingNextPage = toCopyFrom.isLoadingNextPage();
      this.nextPageError = toCopyFrom.getNextPageError();

      this.loadingPullToRefresh = toCopyFrom.isLoadingPullToRefresh();
      this.pullToRefreshError = toCopyFrom.getPullToRefreshError();

      this.newComment = toCopyFrom.isNewComment();
      this.newCommentError = toCopyFrom.getNewCommentError();

      this.bookmarkError = toCopyFrom.getBookmarkError();

      this.deleteComment = toCopyFrom.isDeleteComment();
      this.deleteCommentError = toCopyFrom.getDeleteCommentError();

      this.deletePost = toCopyFrom.isDeletePost();
      this.deletePostError = toCopyFrom.getDeletePostError();

      this.votePost = toCopyFrom.isVotePost();
      this.votePostError = toCopyFrom.getVotePostError();

      this.voteComment = toCopyFrom.isVoteComment();
      this.voteCommentError = toCopyFrom.getVoteCommentError();

      this.acceptComment = toCopyFrom.isAcceptComment();
      this.acceptCommentError = toCopyFrom.getAcceptCommentError();
    }

    Builder firstPageLoading(boolean loadingFirstPage) {
      this.loadingFirstPage = loadingFirstPage;
      return this;
    }

    Builder firstPageError(Throwable error) {
      this.firstPageError = error;
      return this;
    }

    Builder data(List<FeedItem<?>> data) {
      this.data = data;
      return this;
    }

    Builder nextPageLoading(boolean loadingNextPage) {
      this.loadingNextPage = loadingNextPage;
      return this;
    }

    Builder nextPageError(Throwable error) {
      this.nextPageError = error;
      return this;
    }

    Builder pullToRefreshLoading(boolean loading) {
      this.loadingPullToRefresh = loading;
      return this;
    }

    Builder pullToRefreshError(Throwable error) {
      this.pullToRefreshError = error;
      return this;
    }

    Builder newComment(boolean newComment) {
      this.newComment = newComment;
      return this;
    }

    Builder newCommentError(Throwable error) {
      this.newCommentError = error;
      return this;
    }

    Builder bookmarkError(Throwable error) {
      this.bookmarkError = error;
      return this;
    }

    Builder deleteComment(boolean deleteComment) {
      this.deleteComment = deleteComment;
      return this;
    }

    Builder deleteCommentError(Throwable error) {
      this.deleteCommentError = error;
      return this;
    }

    Builder deletePost(boolean deletePost) {
      this.deletePost = deletePost;
      return this;
    }

    Builder deletePostError(Throwable error) {
      this.deletePostError = error;
      return this;
    }

    Builder votePost(boolean votePost) {
      this.votePost = votePost;
      return this;
    }

    Builder votePostError(Throwable error) {
      this.votePostError = error;
      return this;
    }

    Builder voteComment(boolean voteComment) {
      this.voteComment = voteComment;
      return this;
    }

    Builder voteCommentError(Throwable error) {
      this.voteCommentError = error;
      return this;
    }

    Builder acceptComment(boolean acceptComment) {
      this.acceptComment = acceptComment;
      return this;
    }

    Builder acceptCommentError(Throwable error) {
      this.acceptCommentError = error;
      return this;
    }

    PostDetailViewState build() {
      return new PostDetailViewState(data, loadingFirstPage, firstPageError, loadingNextPage,
          nextPageError, loadingPullToRefresh, pullToRefreshError, newComment, newCommentError,
          bookmarkError, deleteComment, deleteCommentError, deletePost, deletePostError, votePost,
          votePostError, voteComment, voteCommentError, acceptComment, acceptCommentError);
    }
  }
}
