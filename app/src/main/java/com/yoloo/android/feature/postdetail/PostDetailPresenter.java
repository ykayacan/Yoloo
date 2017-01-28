package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public PostDetailPresenter(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  public void loadData(String postId) {
    Disposable d = postRepository.get(postId)
        .doOnSubscribe(disposable -> getView().onLoading(true))
        .flatMap(post ->
            Observable.zip(
                Observable.just(post),
                post.getAcceptedCommentId() == null
                    ? Observable.just(new CommentRealm())
                    : commentRepository.get(post.getAcceptedCommentId()),
                commentRepository.list(postId, null, null, 20),
                userRepository.getLocalMe(),
                Group.Of4::create))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(group -> {
          getView().onLoading(false);
          getView().onPostLoaded(group.first);

          if (group.second.getId() != null) {
            getView().onAcceptedCommentLoaded(group.second);
          }

          getView().onLoaded(group.third);
        }, this::showError);

    getDisposable().add(d);
  }

  public void deletePost(String postId) {
    Disposable d =
        postRepository.delete(postId).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }

  public void loadAcceptedComment(String commentId) {
    if (commentId == null) {
      return;
    }

    Disposable d = commentRepository.get(commentId)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showAcceptedComment, this::showError);

    getDisposable().add(d);
  }

  public void loadComments(boolean pullToRefresh, String postId, String cursor, String eTag,
      int limit) {
    Disposable d = commentRepository.list(postId, cursor, eTag, limit)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showComments, this::showError);

    getDisposable().add(d);
  }

  public void sendComment(CommentRealm comment) {
    Disposable d = commentRepository.add(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showComment, this::showError);

    getDisposable().add(d);
  }

  public void votePost(String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> postRepository.get(postId)
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribe(post -> getView().onPostUpdated(post)), this::showError);

    getDisposable().add(d);
  }

  public void voteComment(String commentId, int direction) {
    Disposable d = commentRepository.vote(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  public void suggestUser(String query) {
    Disposable d = userRepository.search(query, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestions, this::showError);

    getDisposable().add(d);
  }

  private void showComments(Response<List<CommentRealm>> response) {
    getView().onLoading(false);
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