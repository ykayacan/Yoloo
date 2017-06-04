package com.yoloo.android.feature.blog;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

/**
 * The type Blog presenter.
 */
class BlogPresenter extends MvpPresenter<BlogView> {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  private String cursor;

  private PostRealm post;
  private AccountRealm me;

  /**
   * Instantiates a new Blog presenter.
   *
   * @param postRepository the post repository
   * @param commentRepository the comment repository
   * @param userRepository the user repository
   */
  BlogPresenter(PostRepository postRepository, CommentRepository commentRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
  }

  /**
   * Load comments.
   *
   * @param post the post
   */
  void loadComments(@Nonnull final PostRealm post) {
    Disposable d = Observable
        .zip(getMeObservable(), getCommentsObservable(post.getId()), Pair::create)
        .map(pair -> {

          List<CommentRealm> comments = Stream
              .of(pair.second.getData())
              .map(comment -> processComment(pair, post, comment))
              .toList();

          return Group.Of3.create(pair.first, comments, pair.second.getCursor());
        })
        .subscribe(group -> {
          this.me = group.first;
          this.post = post;

          getView().onLoaded(group.second);

          cursor = group.third;
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Load more comments.
   */
  void loadMoreComments() {
    Observable<AccountRealm> meObservable = getMeObservable();

    Observable<Response<List<CommentRealm>>> commentsObservable =
        getCommentsObservable(post.getId());

    Disposable d = Observable.zip(meObservable, commentsObservable, Pair::create).map(pair -> {
      List<CommentRealm> comments = Stream
          .of(pair.second.getData())
          .map(comment -> comment
              .setOwner(pair.first.getId().equals(comment.getOwnerId()))
              .setPostAccepted(post.getAcceptedCommentId() != null)
              .setPostOwner(post.getOwnerId().equals(pair.first.getId())))
          .toList();

      return Response.create(comments, pair.second.getCursor());
    }).subscribe(response -> {
      cursor = response.getCursor();

      getView().onLoaded(response.getData());
    }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Vote comment.
   *
   * @param commentId the comment id
   * @param direction the direction
   */
  void voteComment(@Nonnull String commentId, int direction) {
    Disposable d = commentRepository
        .voteComment(commentId, direction)
        .map(this::setExtraCommentProperties)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(comment -> {
          getView().onCommentUpdated(comment);
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Accept comment.
   *
   * @param comment the comment
   */
  void acceptComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .acceptComment(comment)
        .map(this::setExtraCommentProperties)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(updated -> getView().onCommentUpdated(updated),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Delete comment.
   *
   * @param comment the comment
   */
  void deleteComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .deleteComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  void deletePost(@Nonnull String postId) {
    Disposable d =
        postRepository.deletePost(postId).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }

  /**
   * Vote post.
   *
   * @param postId the post id
   * @param direction the direction
   */
  void votePost(@Nonnull String postId, int direction) {
    Disposable d = postRepository
        .votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Bookmark post.
   *
   * @param postId the post id
   */
  void bookmarkPost(String postId) {
    Disposable d = postRepository
        .bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  /**
   * Un bookmark post.
   *
   * @param postId the post id
   */
  void unBookmarkPost(String postId) {
    Disposable d = postRepository
        .unBookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Response<List<CommentRealm>>> getCommentsObservable(@Nonnull String postId) {
    return commentRepository
        .listComments(postId, cursor, 30)
        .observeOn(AndroidSchedulers.mainThread(), true);
  }

  private CommentRealm processComment(Pair<AccountRealm, Response<List<CommentRealm>>> pair,
      PostRealm post, CommentRealm comment) {
    return comment
        .setPostType(post.getPostType())
        .setOwner(pair.first.getId().equals(comment.getOwnerId()))
        .setPostAccepted(post.getAcceptedCommentId() != null)
        .setPostOwner(post.getOwnerId().equals(pair.first.getId()));
  }

  private CommentRealm setExtraCommentProperties(CommentRealm comment) {
    return comment.setOwner(comment.getOwnerId().equals(me.getId()))
        .setPostType(post.getPostType())
        .setPostAccepted(post.getAcceptedCommentId() != null)
        .setPostOwner(post.getOwnerId().equals(comment.getOwnerId()));
  }
}
