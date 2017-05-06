package com.yoloo.android.feature.postdetail;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.FeedItem;
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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private String cursor;
  private PostRealm post;

  PostDetailPresenter(CommentRepository commentRepository, PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  void loadData(boolean pullToRefresh, @Nonnull String postId) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Disposable d = Observable
        .zip(getMeObservable(), getPostObservable(postId), getCommentsObservable(postId),
            Group.Of3::create)
        .map(group -> {
          List<FeedItem> items = new ArrayList<>();
          items.add(new PostFeedItem(group.second));

          List<CommentFeedItem> comments = Stream
              .of(group.third.getData())
              .map(comment -> processComment(group, comment))
              .map(CommentFeedItem::new)
              .toList();

          items.addAll(comments);

          return Group.Of4.create(group.first, group.second, items, group.third.getCursor());
        })
        .subscribe(group -> {
          getView().onMeLoaded(group.first);
          getView().onPostLoaded(group.second);
          getView().onLoaded(group.third);

          post = group.second;
          cursor = group.fourth;
        }, this::showError);

    getDisposable().add(d);
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

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Response<List<CommentRealm>>> getCommentsObservable(@Nonnull String postId) {
    return commentRepository
        .listComments(postId, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread(), true);
  }

  private Observable<PostRealm> getPostObservable(@Nonnull String postId) {
    return postRepository
        .getPost(postId)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

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

      getView().onLoaded(new ArrayList<>(response.getData()));
    }, this::showError);

    getDisposable().add(d);
  }

  void deletePost(@Nonnull String postId) {
    Disposable d =
        postRepository.deletePost(postId).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }

  void acceptComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .acceptComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> getView().onCommentAccepted(__.getId()), this::showError);

    getDisposable().add(d);
  }

  void deleteComment(@Nonnull CommentRealm comment) {
    Disposable d = commentRepository
        .deleteComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void votePost(@Nonnull String postId, int direction) {
    Disposable d = postRepository
        .votePost(postId, direction)
        .andThen(postRepository.getPost(postId))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .subscribe(post -> getView().onPostUpdated(post), this::showError);

    getDisposable().add(d);
  }

  void voteComment(@Nonnull String commentId, int direction) {
    Disposable d = commentRepository
        .voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, this::showError);

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
}
