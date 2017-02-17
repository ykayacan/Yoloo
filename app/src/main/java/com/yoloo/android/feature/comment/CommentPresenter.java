package com.yoloo.android.feature.comment;

import com.annimon.stream.Optional;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class CommentPresenter extends MvpPresenter<CommentView> {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  CommentPresenter(CommentRepository commentRepository, UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  void loadData(String postId, String acceptedCommentId) {
    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            getAcceptedCommentObservable(acceptedCommentId),
            commentRepository.listComments(postId, null, null, 20),
            Group.Of3::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> {
          getView().onAccountLoaded(group.first);

          if (group.second.isPresent()) {
            getView().onAcceptedCommentLoaded(group.second.get());
          }

          getView().onLoaded(group.third);
        }, this::showError);

    getDisposable().add(d);
  }

  void loadComments(boolean pullToRefresh, String postId, String cursor, String eTag, int limit) {
    Disposable d = commentRepository.listComments(postId, cursor, eTag, limit)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(response -> getView().onLoaded(response), this::showError);

    getDisposable().add(d);
  }

  void voteComment(String commentId, int direction) {
    Disposable d = commentRepository.voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  void acceptComment(CommentRealm comment) {
    Disposable d = commentRepository.acceptComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(c -> getView().onNewAccept(c.getId()), this::showError);

    getDisposable().add(d);
  }

  void deleteComment(CommentRealm comment) {
    Disposable d = commentRepository.deleteComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private Observable<Optional<CommentRealm>> getAcceptedCommentObservable(
      String acceptedCommentId) {
    return acceptedCommentId == null
        ? Observable.just(Optional.empty())
        : commentRepository.getComment(acceptedCommentId).map(Optional::of);
  }
}
