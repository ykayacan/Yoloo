package com.yoloo.android.feature.postdetail;

import com.annimon.stream.Optional;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  PostDetailPresenter(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  void loadData(boolean pullToRefresh, String postId, String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            postRepository.getPost(postId)
                .flatMap(post -> Observable.zip(
                    Observable.just(post),
                    getAcceptedCommentObservable(post),
                    Pair::create)),
            commentRepository.listComments(postId, cursor, eTag, limit),
            Group.Of3::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> {
          getView().onAccountLoaded(group.first);
          getView().onPostLoaded(group.second.first);

          if (group.second.first.getAcceptedCommentId() != null
              && group.second.second.isPresent()) {
            getView().onAcceptedCommentLoaded(group.second.second.get());
          }

          if (group.third.getData().isEmpty()) {
            getView().onEmpty();
          } else {
            getView().onLoaded(group.third);
          }
        }, this::showError);

    getDisposable().add(d);
  }

  void deletePost(String postId) {
    Disposable d = postRepository
        .deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
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

  void votePost(String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> postRepository.getPost(postId)
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribe(post -> getView().onPostUpdated(post)), this::showError);

    getDisposable().add(d);
  }

  void voteComment(String commentId, int direction) {
    Disposable d = commentRepository.voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private Observable<Optional<CommentRealm>> getAcceptedCommentObservable(PostRealm post) {
    return post.getAcceptedCommentId() == null
        ? Observable.just(Optional.empty())
        : commentRepository.getComment(post.getAcceptedCommentId()).map(Optional::of);
  }
}