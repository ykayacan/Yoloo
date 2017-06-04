package com.yoloo.android.feature.postdetail.mvi;

import com.annimon.stream.Stream;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import java.util.ArrayList;
import java.util.List;

interface PartialState {

  PostDetailViewState computeNewState(PostDetailViewState previousState);

  /**
   * Indicates that the first page is loading
   */
  final class FirstPageLoading implements PartialState {

    @Override public String toString() {
      return "FirstPageLoadingState{}";
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().firstPageLoading(true).build();
    }
  }

  /**
   * Indicates that an error has occurred while loading the first page
   */
  final class FirstPageError implements PartialState {
    private final Throwable error;

    FirstPageError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public String toString() {
      return "FirstPageErrorState{" + "error=" + error + '}';
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().firstPageLoading(false).firstPageError(error).build();
    }
  }

  /**
   * Indicates that the first page data has been loaded successfully
   */
  final class FirstPageLoaded implements PartialState {
    private final List<FeedItem<?>> data;

    FirstPageLoaded(List<FeedItem<?>> data) {
      this.data = data;
    }

    List<FeedItem<?>> getData() {
      return data;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().firstPageLoading(false).data(data).build();
    }
  }

  /**
   * Indicates that loading the next page has started
   */
  final class NextPageLoading implements PartialState {
    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().nextPageLoading(true).build();
    }
  }

  /**
   * Error while loading new page
   */
  final class NexPageLoadingError implements PartialState {
    private final Throwable error;

    NexPageLoadingError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().nextPageLoading(false).nextPageError(error).build();
    }
  }

  /**
   * Next Page has been loaded successfully
   */
  final class NextPageLoaded implements PartialState {
    private final List<FeedItem<?>> data;

    NextPageLoaded(List<FeedItem<?>> data) {
      this.data = data;
    }

    List<FeedItem<?>> getData() {
      return data;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = new ArrayList<>(previousState.getData().size() + getData().size());
      data.addAll(previousState.getData());
      data.addAll(this.data);

      return previousState.builder().nextPageLoading(false).data(data).build();
    }
  }

  /**
   * Indicates that loading the newest items via pull to refresh has started
   */
  final class PullToRefreshLoading implements PartialState {
    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().pullToRefreshLoading(true).build();
    }
  }

  /**
   * Indicates that an error while loading the newest items via pull to refresh has occurred
   */
  final class PullToRefreshLoadingError implements PartialState {
    private final Throwable error;

    PullToRefreshLoadingError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().pullToRefreshLoading(false).pullToRefreshError(error).build();
    }
  }

  /**
   * Indicates that data has been loaded successfully over pull-to-refresh
   */
  final class PullToRefreshLoaded implements PartialState {
    private final List<FeedItem<?>> data;

    PullToRefreshLoaded(List<FeedItem<?>> data) {
      this.data = data;
    }

    List<FeedItem<?>> getData() {
      return data;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().pullToRefreshLoading(false).data(data).build();
    }
  }

  final class NewComment implements PartialState {
    private final CommentRealm comment;

    NewComment(CommentRealm comment) {
      this.comment = comment;
    }

    CommentRealm getComment() {
      return comment;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = new ArrayList<>(previousState.getData().size() + 1);
      data.addAll(previousState.getData());
      data.add(new CommentFeedItem(comment));
      return previousState.builder().newComment(true).data(data).build();
    }
  }

  final class NewCommentError implements PartialState {
    private final Throwable error;

    NewCommentError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().newComment(false).newCommentError(error).build();
    }
  }

  final class Bookmark implements PartialState {
    private final PostRealm post;

    Bookmark(PostRealm post) {
      this.post = post;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = new ArrayList<>(previousState.getData().size());
      data.add(post.mapToFeedItem());
      data.addAll(previousState.getData().subList(1, previousState.getData().size()));

      return previousState.builder().data(data).build();
    }
  }

  final class BookmarkError implements PartialState {
    private final Throwable error;

    BookmarkError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().bookmarkError(error).build();
    }
  }

  final class DeletePost implements PartialState {
    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().deletePost(true).build();
    }
  }

  final class DeletePostError implements PartialState {
    private final Throwable error;

    DeletePostError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().deletePost(false).deletePostError(error).build();
    }
  }

  final class DeleteComment implements PartialState {
    private final CommentRealm comment;

    DeleteComment(CommentRealm comment) {
      this.comment = comment;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = new ArrayList<>(previousState.getData().size());
      data.remove(new CommentFeedItem(comment));

      return previousState.builder().deleteComment(true).data(data).build();
    }
  }

  final class DeleteCommentError implements PartialState {
    private final Throwable error;

    DeleteCommentError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().deleteComment(false).deleteCommentError(error).build();
    }
  }

  final class VotePost implements PartialState {
    private final PostRealm post;

    VotePost(PostRealm post) {
      this.post = post;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = new ArrayList<>(previousState.getData().size());
      data.add(post.mapToFeedItem());
      data.addAll(previousState.getData().subList(1, previousState.getData().size()));
      return previousState.builder().data(data).votePost(true).build();
    }
  }

  final class VotePostError implements PartialState {
    private final Throwable error;

    VotePostError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().votePost(false).votePostError(error).build();
    }
  }

  final class VoteComment implements PartialState {
    private final CommentRealm comment;

    VoteComment(CommentRealm comment) {
      this.comment = comment;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = previousState.getData();
      Stream.of(data).forEachIndexed((index, feedItem) -> {
        if (data.get(index).getId().equals(comment.getId())) {
          data.set(index, new CommentFeedItem(comment));
        }
      });

      return previousState.builder().data(data).voteComment(true).build();
    }
  }

  final class VoteCommentError implements PartialState {
    private final Throwable error;

    VoteCommentError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().voteComment(false).voteCommentError(error).build();
    }
  }

  final class AcceptComment implements PartialState {
    private final CommentRealm comment;

    AcceptComment(CommentRealm comment) {
      this.comment = comment;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      List<FeedItem<?>> data = previousState.getData();
      Stream.of(data).forEachIndexed((index, feedItem) -> {
        if (feedItem.getId().equals(comment.getId())) {
          data.set(index, new CommentFeedItem(comment));
        }
      });

      return previousState.builder().data(data).acceptComment(true).build();
    }
  }

  final class AcceptCommentError implements PartialState {
    private final Throwable error;

    AcceptCommentError(Throwable error) {
      this.error = error;
    }

    Throwable getError() {
      return error;
    }

    @Override public PostDetailViewState computeNewState(PostDetailViewState previousState) {
      return previousState.builder().acceptComment(false).acceptCommentError(error).build();
    }
  }
}
