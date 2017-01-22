package com.yoloo.android.feature.comment;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class CommentPresenter extends MvpPresenter<CommentView> {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  public CommentPresenter(CommentRepository commentRepository, UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  void loadAcceptedComment(String commentId) {
    if (commentId == null) {
      return;
    }

    Disposable d = commentRepository.get(commentId)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showAcceptedComment, this::showError);

    getDisposable().add(d);
  }

  void loadComments(boolean pullToRefresh, String postId, String cursor, String eTag, int limit) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = commentRepository.list(postId, cursor, eTag, limit)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showComments, this::showError);

    getDisposable().add(d);
  }

  void sendComment(CommentRealm comment) {
    Disposable d = commentRepository.add(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showComment, this::showError);

    getDisposable().add(d);
  }

  void voteComment(String commentId, int direction) {
    Disposable d = commentRepository.vote(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  void suggestUser(String filtered) {
    Disposable d = userRepository.list(filtered, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestions, this::showError);

    getDisposable().add(d);
  }

  private void showComments(Response<List<CommentRealm>> response) {
    getView().onLoaded(response);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void showAcceptedComment(CommentRealm comment) {
    getView().onAcceptedCommentLoaded(comment);
  }

  private void showComment(CommentRealm comment) {
    getView().onCommentLoaded(comment);
  }

  private void showSuggestions(Response<List<AccountRealm>> response) {
    getView().onMentionSuggestionsLoaded(response.getData());
  }
}
