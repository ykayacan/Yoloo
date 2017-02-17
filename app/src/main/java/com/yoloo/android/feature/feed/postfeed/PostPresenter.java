package com.yoloo.android.feature.feed.postfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

class PostPresenter extends MvpPresenter<PostView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  PostPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  void loadPostsByCategory(boolean pullToRefresh, String categoryName, PostSorter sorter,
      String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            postRepository.listByCategory(categoryName, sorter, cursor, eTag, limit),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  void loadPostsByTag(boolean pullToRefresh, String tagName, PostSorter sorter,
      String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            postRepository.listByTags(tagName, sorter, cursor, eTag, limit),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  void loadPostsByUser(boolean pullToRefresh, String userId, boolean commented,
      String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            postRepository.listByUser(userId, commented, cursor, eTag, limit),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  void loadPostsByBounty(boolean pullToRefresh, String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            postRepository.listByBounty(cursor, eTag, limit),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  void loadPostsByBookmarked(boolean pullToRefresh, String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            postRepository.listByBookmarked(cursor, eTag, limit),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  void deletePost(String postId) {
    Disposable d = postRepository.deletePost(postId)
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

  void bookmarkPost(String postId) {
    Disposable d = postRepository.bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void unBookmarkPost(String postId) {
    Disposable d = postRepository.unBookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showData(Pair<AccountRealm, Response<List<PostRealm>>> pair) {
    getView().onAccountLoaded(pair.first);

    if (pair.second.getData().isEmpty()) {
      getView().onEmpty();
    } else {
      getView().onLoaded(pair.second);
    }

    getView().showContent();
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}