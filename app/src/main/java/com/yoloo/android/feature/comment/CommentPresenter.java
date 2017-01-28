package com.yoloo.android.feature.comment;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class CommentPresenter extends MvpPresenter<CommentView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private boolean self;
  private boolean hasAcceptedId;
  private long totalComments;

  public CommentPresenter(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  void loadPostAndComments(String postId) {
    Disposable d = postRepository.get(postId)
        .flatMap(post -> Observable.zip(
            Observable.just(post),
            getAcceptedCommentObservable(post),
            commentRepository.list(postId, null, null, 20),
            userRepository.getLocalMe(),
            Group.Of4::create))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> {
          self = group.fourth.getId().equals(group.first.getOwnerId());
          hasAcceptedId = group.first.getAcceptedCommentId() != null;
          totalComments = group.first.getComments();

          if (group.second.getId() != null) {
            getView().onAcceptedCommentLoaded(group.second, self, hasAcceptedId);
          }
          getView().onCommentsLoaded(group.third, self, hasAcceptedId, totalComments);
        }, this::showError);

    getDisposable().add(d);
  }

  void loadComments(boolean pullToRefresh, String postId, String cursor, String eTag, int limit) {
    Disposable d = commentRepository.list(postId, cursor, eTag, limit)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showComments, this::showError);

    getDisposable().add(d);
  }

  void sendComment(CommentRealm comment) {
    Disposable d = commentRepository.add(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showNewComment, this::showError);

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
    Disposable d = userRepository.search(filtered, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestions, this::showError);

    getDisposable().add(d);
  }

  void acceptComment(String postId, String commentId) {
    Disposable d = commentRepository.accept(postId, commentId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showComments(Response<List<CommentRealm>> response) {
    getView().onCommentsLoaded(response, self, hasAcceptedId, totalComments);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void showNewComment(CommentRealm comment) {
    totalComments += 1;
    getView().onNewCommentLoaded(comment, self, hasAcceptedId, totalComments);
  }

  private void showSuggestions(Response<List<AccountRealm>> response) {
    getView().onMentionSuggestionsLoaded(response.getData());
  }

  private Observable<CommentRealm> getAcceptedCommentObservable(PostRealm post) {
    return post.getAcceptedCommentId() == null ? Observable.just(new CommentRealm())
        : commentRepository.get(post.getAcceptedCommentId());
  }
}
