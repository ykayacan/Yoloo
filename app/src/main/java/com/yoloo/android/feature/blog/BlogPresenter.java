package com.yoloo.android.feature.blog;

import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class BlogPresenter extends MvpPresenter<BlogView> {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  private String cursor;

  BlogPresenter(PostRepository postRepository, CommentRepository commentRepository) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
  }

  void loadComments(@Nonnull String postId) {
    Disposable d = commentRepository
        .listComments(postId, cursor, 30)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          cursor = response.getCursor();
          getView().onLoaded(response.getData());
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void voteComment(@Nonnull String commentId, int direction) {
    Disposable d = commentRepository
        .voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void acceptComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .acceptComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(acceptedComment -> getView().onCommentAccepted(acceptedComment),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void deleteComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .deleteComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
