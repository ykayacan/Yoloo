package com.yoloo.android.feature.postdetail.mvi;

import com.annimon.stream.Stream;
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

class PostDetailMviPresenter extends MviBasePresenter<PostDetailMviView, PostDetailViewState> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;

  private PostRealm post;
  private String cursor;

  PostDetailMviPresenter(CommentRepository commentRepository, PostRepository postRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
  }

  @Override protected void bindIntents() {

    List<Observable<PartialState>> states = new ArrayList<>(10);

    states.add(processLoadFirstPageIntent());
    states.add(processLoadNextPageIntent());
    states.add(processPullToRefreshIntent());
    states.add(processNewCommentIntent());
    states.add(processBookmarkIntent());
    states.add(processDeletePostIntent());
    states.add(processDeleteCommentIntent());
    states.add(processVotePostIntent());
    states.add(processVoteCommentIntent());
    states.add(processAcceptCommentIntent());

    Observable<PartialState> allIntentsObservable =
        Observable.merge(states).observeOn(AndroidSchedulers.mainThread());

    PostDetailViewState initialState =
        new PostDetailViewState.Builder().firstPageLoading(true).build();

    subscribeViewState(allIntentsObservable.scan(initialState,
        (viewState, partialState) -> partialState.computeNewState(viewState))
        .distinctUntilChanged(), PostDetailMviView::render);
  }

  private Observable<PartialState> processPullToRefreshIntent() {
    return intent(PostDetailMviView::pullToRefreshIntent).doOnNext(
        ignored -> Timber.d("intent: pull to refresh"))
        .doOnNext(ignored -> cursor = null)
        .flatMap(postId -> loadFirstPage(postId).map(
            items -> (PartialState) new PartialState.PullToRefreshLoaded(items))
            .startWith(new PartialState.PullToRefreshLoading())
            .onErrorReturn(PartialState.PullToRefreshLoadingError::new));
  }

  private Observable<PartialState> processLoadNextPageIntent() {
    return intent(PostDetailMviView::loadNextPageIntent).doOnNext(
        ignored -> Timber.d("intent: load next page"))
        .flatMap(ignored -> loadNextPage().map(
            items -> (PartialState) new PartialState.NextPageLoaded(items))
            .startWith(new PartialState.NextPageLoading())
            .onErrorReturn(PartialState.NexPageLoadingError::new));
  }

  private Observable<PartialState> processLoadFirstPageIntent() {
    return intent(PostDetailMviView::loadFirstPageIntent).doOnNext(
        ignored -> Timber.d("intent: load first page"))
        .flatMap(postId -> loadFirstPage(postId).map(
            items -> (PartialState) new PartialState.FirstPageLoaded(items))
            .startWith(new PartialState.FirstPageLoading())
            .onErrorReturn(PartialState.FirstPageError::new));
  }

  private Observable<PartialState> processNewCommentIntent() {
    return intent(PostDetailMviView::newCommentIntent).doOnNext(
        comment -> Timber.d("intent: new comment"))
        .map(comment -> comment.processPostData(post))
        .map(comment -> (PartialState) new PartialState.NewComment(comment))
        .onErrorReturn(PartialState.NewCommentError::new);
  }

  private Observable<PartialState> processBookmarkIntent() {
    return intent(PostDetailMviView::bookmarkIntent).doOnNext(
        ignored -> Timber.d("intent: bookmark"))
        .flatMap(oldPost -> bookmarkOrUnbookmark(oldPost).toObservable()
            .doOnNext(post -> this.post = post)
            .map(post -> (PartialState) new PartialState.Bookmark(post))
            .onErrorReturn(PartialState.BookmarkError::new));
  }

  private Observable<PartialState> processDeletePostIntent() {
    return intent(PostDetailMviView::deletePostIntent).doOnNext(
        ignored -> Timber.d("intent: delete post"))
        .flatMap(post -> postRepository.deletePost(post.getId())
            .toObservable()
            .map(ignored -> (PartialState) new PartialState.DeletePost())
            .onErrorReturn(PartialState.DeletePostError::new));
  }

  private Observable<PartialState> processDeleteCommentIntent() {
    return intent(PostDetailMviView::deleteCommentIntent).doOnNext(
        ignored -> Timber.d("intent: delete comment"))
        .flatMap(comment -> commentRepository.deleteComment(comment)
            .toObservable()
            .map(ignored -> (PartialState) new PartialState.DeleteComment(comment))
            .onErrorReturn(PartialState.DeleteCommentError::new));
  }

  private Observable<PartialState> processVotePostIntent() {
    return intent(PostDetailMviView::votePostIntent).doOnNext(
        ignored -> Timber.d("intent: vote post"))
        .flatMap(pair -> postRepository.votePost(pair.first.getId(), pair.second)
            .toObservable()
            .doOnNext(post -> this.post = post)
            .map(post -> (PartialState) new PartialState.VotePost(post))
            .onErrorReturn(PartialState.VotePostError::new));
  }

  private Observable<PartialState> processVoteCommentIntent() {
    return intent(PostDetailMviView::voteCommentIntent).doOnNext(
        ignored -> Timber.d("intent: vote comment"))
        .flatMap(pair -> commentRepository.voteComment(pair.first.getId(), pair.second)
            .toObservable()
            .map(comment -> comment.processPostData(post))
            .map(comment -> (PartialState) new PartialState.VoteComment(comment))
            .onErrorReturn(PartialState.VoteCommentError::new));
  }

  private Observable<PartialState> processAcceptCommentIntent() {
    return intent(PostDetailMviView::acceptCommentIntent).doOnNext(
        ignored -> Timber.d("intent: accept comment"))
        .flatMap(comment -> commentRepository.acceptComment(comment)
            .toObservable()
            .map(acceptedComment -> acceptedComment.processPostData(
                post.setAcceptedCommentId(acceptedComment.getId())))
            .map(acceptedComment -> (PartialState) new PartialState.AcceptComment(acceptedComment))
            .onErrorReturn(PartialState.AcceptCommentError::new));
  }

  private Observable<List<FeedItem<?>>> loadFirstPage(String postId) {
    return Observable.zip(getPostObservable(postId), getCommentsObservable(postId), Pair::create)
        .doOnNext(pair -> post = pair.first)
        .doOnNext(pair -> cursor = pair.second.getCursor())
        .map(pair -> {
          List<FeedItem<?>> items = new ArrayList<>(pair.second.getData().size() + 1);

          items.add(pair.first.mapToFeedItem());

          Stream.of(pair.second.getData())
              .map(comment -> comment.processPostData(pair.first))
              .map(CommentFeedItem::new)
              .forEach(items::add);

          return items;
        });
  }

  private Observable<Response<List<CommentRealm>>> getCommentsObservable(@Nonnull String postId) {
    return commentRepository.listComments(postId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<PostRealm> getPostObservable(@Nonnull String postId) {
    return postRepository.getPost(postId).toObservable();
  }

  private Observable<ArrayList<FeedItem<?>>> loadNextPage() {
    return getCommentsObservable(post.getId()).doOnNext(response -> cursor = response.getCursor())
        .map(Response::getData)
        .flatMap(Observable::fromIterable)
        .map(comment -> comment.processPostData(post))
        .map(CommentFeedItem::new)
        .toList()
        .map(ArrayList<FeedItem<?>>::new)
        .toObservable();
  }

  private Single<PostRealm> bookmarkOrUnbookmark(PostRealm old) {
    return old.isBookmarked() ? postRepository.unBookmarkPost(old.getId())
        : postRepository.bookmarkPost(old.getId());
  }
}
