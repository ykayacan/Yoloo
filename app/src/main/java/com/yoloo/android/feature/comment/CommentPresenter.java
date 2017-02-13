package com.yoloo.android.feature.comment;

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
import java.util.List;

public class CommentPresenter extends MvpPresenter<CommentView> {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  private boolean postOwner;
  private boolean accepted;
  private String currentUserId;

  public CommentPresenter(CommentRepository commentRepository, UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  void loadData(String postId, String postOwnerId, String acceptedCommentId) {
    Disposable d = Observable
        .zip(getAcceptedCommentObservable(acceptedCommentId),
            commentRepository.listComments(postId, null, null, 20),
            userRepository.getLocalMe(),
            Group.Of3::create)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(group -> {
          AccountRealm account = group.third;

          this.postOwner = account.getId().equals(postOwnerId);
          this.currentUserId = account.getId();
          this.accepted = acceptedCommentId != null;

          if (accepted) {
            getView().onAcceptedCommentLoaded(group.first, postOwner);
          }

          getView().onCommentsLoaded(group.second, currentUserId, postOwner, accepted);
        }, this::showError);

    getDisposable().add(d);
  }

  void loadComments(boolean pullToRefresh, String postId, String cursor, String eTag, int limit) {
    Disposable d = commentRepository.listComments(postId, cursor, eTag, limit)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(this::showComments, this::showError);

    getDisposable().add(d);
  }

  void sendComment(CommentRealm comment) {
    Disposable d = commentRepository.addComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showNewComment, this::showError);

    getDisposable().add(d);
  }

  void voteComment(String commentId, int direction) {
    Disposable d = commentRepository.voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  void suggestUser(String filtered) {
    Disposable d = userRepository.searchUser(filtered, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestions, this::showError);

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

  private void showComments(Response<List<CommentRealm>> response) {
    getView().onCommentsLoaded(response, currentUserId, postOwner, accepted);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void showNewComment(CommentRealm comment) {
    getView().onNewCommentLoaded(comment, postOwner);
  }

  private void showSuggestions(Response<List<AccountRealm>> response) {
    getView().onMentionSuggestionsLoaded(response.getData());
  }

  private Observable<CommentRealm> getAcceptedCommentObservable(String acceptedCommentId) {
    return acceptedCommentId == null
        ? Observable.just(new CommentRealm())
        : commentRepository.getComment(acceptedCommentId);
  }
}
