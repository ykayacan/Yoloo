package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  PostDetailPresenter(CommentRepository commentRepository, PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  void loadPost(String postId) {
    Disposable d = postRepository.get(postId)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showPost, this::showError);

    getDisposable().add(d);
  }

  void deletePost(String postId) {
    Disposable d =
        postRepository.delete(postId).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
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

  void votePost(String postId, int direction) {
    Disposable d = postRepository.vote(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> postRepository.get(postId)
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribe(post -> getView().onPostUpdated(post)), this::showError);

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

  private void showPost(PostRealm post) {
    getView().onPostLoaded(post);
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