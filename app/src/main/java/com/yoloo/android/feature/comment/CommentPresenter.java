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

  void loadComments(boolean pullToRefresh, @Nonnull String postId, @Nonnull String postOwnerId,
      boolean hasAcceptedComment) {
    shouldResetCursor(pullToRefresh);

    Observable<AccountRealm> meObservable =
        userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());

    Observable<Response<List<CommentRealm>>> commentsObservable = commentRepository
        .listComments(postId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(response -> Timber.d("Comments: %s", response.getData()));

    Disposable d = Observable.zip(meObservable, commentsObservable, Pair::create).map(pair -> {
      List<CommentRealm> comments = Stream
          .of(pair.second.getData())
          .map(comment -> comment
              .setOwner(pair.first.getId().equals(comment.getOwnerId()))
              .setPostAccepted(hasAcceptedComment)
              .setPostOwner(postOwnerId.equals(pair.first.getId())))
          .toList();

      return Pair.create(pair.first, Response.create(comments, pair.second.getCursor()));
    }).subscribe(pair -> {
      cursor = pair.second.getCursor();
      getView().onLoaded(pair.second.getData());
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
        .subscribe(c -> getView().onCommentAccepted(c), this::showError);

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
}
