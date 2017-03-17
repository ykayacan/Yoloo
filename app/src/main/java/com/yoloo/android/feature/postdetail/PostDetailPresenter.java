package com.yoloo.android.feature.postdetail;

import com.annimon.stream.Optional;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private String cursor;

  PostDetailPresenter(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  void loadData(boolean pullToRefresh, @Nonnull String postId, @Nullable String acceptedCommentId,
      int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<AccountRealm> meObservable =
        userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());

    Observable<PostRealm> postObservable = postRepository.getPost(postId)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .filter(Optional::isPresent)
        .map(Optional::get);

    Observable<Optional<CommentRealm>> acceptedCommentObservable =
        getAcceptedCommentObservable(postId, acceptedCommentId)
            .observeOn(AndroidSchedulers.mainThread(), true);

    Observable<Response<List<CommentRealm>>> commentsObservable =
        commentRepository.listComments(postId, cursor, limit)
            .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(
            meObservable,
            postObservable,
            acceptedCommentObservable,
            commentsObservable,
            Group.Of4::create)
        .subscribe(group -> {
          getView().onAccountLoaded(group.first);
          getView().onPostLoaded(group.second);

          if (group.third.isPresent()) {
            getView().onAcceptedCommentLoaded(group.third.get());
          }

          cursor = group.fourth.getCursor();
          getView().onLoaded(group.fourth.getData());
        }, this::showError);

    getDisposable().add(d);
  }

  void deletePost(@Nonnull String postId) {
    Disposable d = postRepository
        .deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void acceptComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository.acceptComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> getView().onNewAccept(__.getId()), this::showError);

    getDisposable().add(d);
  }

  void deleteComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository.deleteComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void votePost(@Nonnull String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .andThen(postRepository.getPost(postId))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .subscribe(post -> getView().onPostUpdated(post), this::showError);

    getDisposable().add(d);
  }

  void voteComment(@Nonnull String commentId, int direction) {
    Disposable d = commentRepository.voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private Observable<Optional<CommentRealm>> getAcceptedCommentObservable(
      String postId, String acceptedCommentId) {
    return acceptedCommentId == null
        ? Observable.just(Optional.empty())
        : commentRepository.getComment(postId, acceptedCommentId);
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }
}
