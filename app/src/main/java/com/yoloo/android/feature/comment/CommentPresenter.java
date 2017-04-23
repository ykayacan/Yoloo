package com.yoloo.android.feature.comment;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

class CommentPresenter extends MvpPresenter<CommentView> {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  private String cursor;

  CommentPresenter(CommentRepository commentRepository, UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  void loadData(String postId, String acceptedCommentId) {
    Observable<AccountRealm> userObservable =
        userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());

    Observable<Optional<CommentRealm>> acceptedCommentObservable =
        getAcceptedCommentObservable(postId, acceptedCommentId).observeOn(
            AndroidSchedulers.mainThread());

    Observable<Response<List<CommentRealm>>> commentsObservable =
        commentRepository.listComments(postId, null, 20).observeOn(AndroidSchedulers.mainThread());

    Disposable d = Observable
        .zip(userObservable, acceptedCommentObservable, commentsObservable, Group.Of3::create)
        .map(group -> {
          AccountRealm account = group.first;
          List<CommentRealm> commentList = new ArrayList<>(21);

          if (group.second.isPresent()) {
            CommentRealm accepted = group.second.get();
            accepted.setOwner(account.getId().equals(accepted.getOwnerId()));
            commentList.add(accepted);
          }

          List<CommentRealm> comments = Stream
              .of(group.third.getData())
              .map(comment -> comment.setOwner(account.getId().equals(comment.getOwnerId())))
              .toList();
          commentList.addAll(comments);

          return Response.create(commentList, group.third.getCursor());
        })
        .subscribe(response -> {
          cursor = response.getCursor();
          getView().onLoaded(response.getData());
        }, this::showError);

    getDisposable().add(d);
  }

  void loadComments(boolean pullToRefresh, @Nonnull String postId, int limit) {
    shouldResetCursor(pullToRefresh);

    Disposable d = commentRepository
        .listComments(postId, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(response -> {
          cursor = response.getCursor();
          getView().onLoaded(response.getData());
        }, this::showError);

    getDisposable().add(d);
  }

  void voteComment(String commentId, int direction) {
    Disposable d = commentRepository
        .voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  void acceptComment(CommentRealm comment) {
    Disposable d = commentRepository
        .acceptComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(c -> getView().onNewAccept(c.getId()), this::showError);

    getDisposable().add(d);
  }

  void deleteComment(CommentRealm comment) {
    Disposable d = commentRepository
        .deleteComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private Observable<Optional<CommentRealm>> getAcceptedCommentObservable(String postId,
      String acceptedCommentId) {
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
