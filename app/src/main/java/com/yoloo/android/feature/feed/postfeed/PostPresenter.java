package com.yoloo.android.feature.feed.postfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

public class PostPresenter extends MvpPresenter<PostView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public PostPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  public void loadPostsByCategory(boolean pullToRefresh, String categoryName, PostSorter sorter,
      String cursor, String eTag, int limit) {
    Disposable d = Observable
        .zip(
            postRepository.listByCategory(categoryName, sorter, cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  public void loadPostsByTag(boolean pullToRefresh, String tagName, PostSorter sorter,
      String cursor, String eTag, int limit) {
    Disposable d = Observable
        .zip(
            postRepository.listByTags(tagName, sorter, cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  public void loadPostsByUser(boolean pullToRefresh, String userId, boolean commented,
      String cursor, String eTag, int limit) {
    Disposable d = Observable
        .zip(
            postRepository.listByUser(userId, commented, cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  public void loadPostsByBounty(boolean pullToRefresh, String cursor, String eTag, int limit) {
    Disposable d = Observable
        .zip(
            postRepository.listByBounty(cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  public void loadPostsByBookmarked(boolean pullToRefresh, String cursor, String eTag, int limit) {
    Disposable d = Observable
        .zip(
            postRepository.listByBookmarked(cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  public void deletePost(String postId) {
    Disposable d = postRepository.delete(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

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

  public void bookmarkPost(String postId) {
    Disposable d = postRepository.bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showData(Pair<Response<List<PostRealm>>, AccountRealm> pair) {
    getView().onLoading(false);
    getView().onAccountLoaded(pair.second);

    if (pair.first.getData() == null) {
      getView().onEmpty();
    } else {
      getView().onLoaded(pair.first);
    }
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}