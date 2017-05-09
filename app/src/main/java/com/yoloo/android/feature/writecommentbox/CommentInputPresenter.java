package com.yoloo.android.feature.writecommentbox;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class CommentInputPresenter extends MvpPresenter<CommentInputView> {

  private final UserRepository userRepository;
  private final CommentRepository commentRepository;

  CommentInputPresenter(UserRepository userRepository,
      CommentRepository commentRepository) {
    this.userRepository = userRepository;
    this.commentRepository = commentRepository;
  }

  void loadMe() {
    Disposable d = userRepository.getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(me -> getView().onMeLoaded(me), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void sendComment(CommentRealm comment) {
    Disposable d = commentRepository.addComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showNewComment, this::showError);

    getDisposable().add(d);
  }

  void suggestUsers(String query) {
    Disposable d = userRepository.searchUser(query, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestions, this::showError);

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void showNewComment(CommentRealm comment) {
    getView().onNewComment(comment);
  }

  private void showSuggestions(Response<List<AccountRealm>> response) {
    getView().onSuggestionsLoaded(response.getData());
  }
}
