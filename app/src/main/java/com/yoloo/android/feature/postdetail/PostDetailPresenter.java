package com.yoloo.android.feature.postdetail;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CommentRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.comment.CommentRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.feed.common.annotation.PostType;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import timber.log.Timber;

public class PostDetailPresenter extends MvpPresenter<PostDetailView> {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private boolean postOwner;
  private boolean accepted;
  private String currentUserId;
  @PostType private int postType;

  public PostDetailPresenter(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  public void loadData(String postId) {
    Disposable d = Observable
        .zip(
            postRepository.getPost(postId)
                .flatMap(post -> Observable.zip(
                    Observable.just(post),
                    getAcceptedCommentObservable(post),
                    Pair::create)),
            commentRepository.listComments(postId, null, null, 20),
            userRepository.getLocalMe(),
            Group.Of3::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> {
          AccountRealm account = group.third;
          PostRealm post = group.first.first;

          this.postOwner = account.getId().equals(post.getOwnerId());
          this.currentUserId = account.getId();
          this.accepted = post.getAcceptedCommentId() != null;
          this.postType = getPostType(post);

          getView().onPostLoaded(group.first.first);

          if (accepted) {
            if (group.first.second.getId() != null) {
              getView().onAcceptedCommentLoaded(group.first.second, postOwner, postType);
            }
          }

          getView().onCommentsLoaded(group.second, currentUserId, postOwner, accepted, postType);

          Timber.d("Hereeee");
        }, this::showError);

    getDisposable().add(d);
  }

  public void loadComments(boolean pullToRefresh, String postId, String cursor, String eTag,
      int limit) {
    Disposable d = commentRepository.listComments(postId, cursor, eTag, limit)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showComments, this::showError);

    getDisposable().add(d);
  }

  public void deletePost(String postId) {
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

  public void sendComment(CommentRealm comment) {
    Disposable d = commentRepository.addComment(comment)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showNewComment, this::showError);

    getDisposable().add(d);
  }

  public void votePost(String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> postRepository.getPost(postId)
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribe(post -> getView().onPostUpdated(post)), this::showError);

    getDisposable().add(d);
  }

  public void voteComment(String commentId, int direction) {
    Disposable d = commentRepository.voteComment(commentId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(this::showError)
        .subscribe();

    getDisposable().add(d);
  }

  public void suggestUser(String query) {
    Disposable d = userRepository.searchUser(query, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestions, this::showError);

    getDisposable().add(d);
  }

  private void showComments(Response<List<CommentRealm>> response) {
    getView().onCommentsLoaded(response, currentUserId, postOwner, accepted, postType);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private void showNewComment(CommentRealm comment) {
    getView().onNewCommentLoaded(comment, postOwner, postType);
  }

  private void showSuggestions(Response<List<AccountRealm>> response) {
    getView().onMentionSuggestionsLoaded(response.getData());
  }

  private Observable<CommentRealm> getAcceptedCommentObservable(PostRealm post) {
    return post.getAcceptedCommentId() == null
        ? Observable.just(new CommentRealm())
        : commentRepository.getComment(post.getAcceptedCommentId());
  }

  @PostType private int getPostType(PostRealm post) {
    switch (post.getType()) {
      case 0:
        return PostType.TYPE_NORMAL;
      case 1:
        return PostType.TYPE_RICH;
      case 2:
        return PostType.TYPE_BLOG;
      default:
        return PostType.TYPE_NORMAL;
    }
  }
}