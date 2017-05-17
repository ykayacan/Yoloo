package com.yoloo.android.feature.comment;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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

  void loadComments(boolean pullToRefresh,
      boolean loadingMore,
      @Nonnull String postId,
      @Nonnull String postOwnerId,
      boolean hasAcceptedComment,
      int postType) {
    shouldResetCursor(pullToRefresh);

    Disposable d =
        Observable.zip(getMeObservable(), getCommentsObservable(postId), Pair::create).map(pair -> {
          List<CommentRealm> comments =
              setExtraCommentProperties(postOwnerId, hasAcceptedComment, postType, pair);

          return Pair.create(pair.first, Response.create(comments, pair.second.getCursor()));
        }).subscribe(pair -> {
          cursor = pair.second.getCursor();
          if (loadingMore) {
            getView().onMoreLoaded(pair.second.getData());
          } else {
            getView().onLoaded(pair.second.getData());
          }
        }, this::showError);

    getDisposable().add(d);
  }

  void voteComment(String commentId, int direction) {
    Disposable d = commentRepository
        .voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(comment -> getView().onCommentUpdated(comment), Timber::e);

    getDisposable().add(d);
  }

  void acceptComment(CommentRealm comment) {
    Disposable d = commentRepository
        .acceptComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(c -> getView().onCommentUpdated(c), this::showError);

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

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }

  private Observable<Response<List<CommentRealm>>> getCommentsObservable(@Nonnull String postId) {
    return commentRepository
        .listComments(postId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private List<CommentRealm> setExtraCommentProperties(@Nonnull String postOwnerId,
      boolean hasAcceptedComment, int postType,
      Pair<AccountRealm, Response<List<CommentRealm>>> pair) {
    return Stream
        .of(pair.second.getData())
        .map(comment -> comment
            .setPostType(postType)
            .setOwner(pair.first.getId().equals(comment.getOwnerId()))
            .setPostAccepted(hasAcceptedComment)
            .setPostOwner(postOwnerId.equals(pair.first.getId())))
        .toList();
  }
}
