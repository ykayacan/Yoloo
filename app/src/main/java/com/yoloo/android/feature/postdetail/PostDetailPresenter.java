package com.yoloo.android.feature.postdetail;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CommentRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.CommentFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

/**
 * The type Post detail presenter.
 */
class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private String cursor;

  private PostRealm post;
  private AccountRealm me;

  /**
   * Instantiates a new Post detail presenter.
   *
   * @param commentRepository the comment repository
   * @param postRepository the post repository
   * @param userRepository the user repository
   */
  PostDetailPresenter(CommentRepository commentRepository, PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  /**
   * Load data.
   *
   * @param pullToRefresh the pull to refresh
   * @param postId the post id
   */
  void loadData(boolean pullToRefresh, @Nonnull String postId) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Disposable d = Observable
        .zip(getMeObservable(), getPostObservable(postId), getCommentsObservable(postId),
            Group.Of3::create)
        .map(group -> {
          List<FeedItem<?>> items = new ArrayList<>();

          PostRealm post = group.second;
          if (post.isTextPost()) {
            items.add(new TextPostFeedItem(post));
          } else if (post.isRichPost()) {
            items.add(new RichPostFeedItem(post));
          } else if (post.isBlogPost()) {
            items.add(new BlogPostFeedItem(post));
          }

          List<CommentFeedItem> comments = Stream
              .of(group.third.getData())
              .map(comment -> processComment(group, comment))
              .map(CommentFeedItem::new)
              .toList();

          items.addAll(comments);

          return Group.Of4.create(group.first, group.second, items, group.third.getCursor());
        })
        .subscribe(group -> {
          me = group.first;

          getView().onPostLoaded(group.second);
          getView().onLoaded(group.third);

          post = group.second;
          cursor = group.fourth;
        }, this::showError);

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
      List<CommentFeedItem> comments = Stream
          .of(pair.second.getData())
          .map(comment -> comment
              .setOwner(pair.first.getId().equals(comment.getOwnerId()))
              .setPostAccepted(post.getAcceptedCommentId() != null)
              .setPostOwner(post.getOwnerId().equals(pair.first.getId())))
          .map(CommentFeedItem::new)
          .toList();

      return Response.create(comments, pair.second.getCursor());
    }).subscribe(response -> {
      cursor = response.getCursor();

      getView().onMoreLoaded(new ArrayList<>(response.getData()));
    }, this::showError);

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
   * Accept comment.
   *
   * @param comment the comment
   */
  void acceptComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .acceptComment(comment)
        .map(this::setExtraCommentProperties)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(updated -> getView().onCommentUpdated(updated), this::showError);

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
        .subscribe();

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
        .doOnSuccess(postRealm -> Timber.d("Updated post: %s", postRealm))
        .subscribe(post -> getView().onPostUpdated(post), this::showError);

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
        .doOnSuccess(comment -> Timber.d("Updated comment: %s", comment))
        .subscribe(comment -> getView().onCommentUpdated(comment),
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

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Response<List<CommentRealm>>> getCommentsObservable(@Nonnull String postId) {
    return commentRepository
        .listComments(postId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<PostRealm> getPostObservable(@Nonnull String postId) {
    return postRepository
        .getPost(postId)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  private CommentRealm processComment(
      Group.Of3<AccountRealm, PostRealm, Response<List<CommentRealm>>> group,
      CommentRealm comment) {
    return comment
        .setPostType(group.second.getPostType())
        .setOwner(group.first.getId().equals(comment.getOwnerId()))
        .setPostAccepted(group.second.getAcceptedCommentId() != null)
        .setPostOwner(group.second.getOwnerId().equals(group.first.getId()));
  }

  private CommentRealm setExtraCommentProperties(CommentRealm comment) {
    return comment.setOwner(comment.getOwnerId().equals(me.getId()))
        .setPostType(post.getPostType())
        .setPostAccepted(post.getAcceptedCommentId() != null)
        .setPostOwner(post.getOwnerId().equals(comment.getOwnerId()));
  }
}
