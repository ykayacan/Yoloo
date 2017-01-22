package com.yoloo.android.feature.feed.globalfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

class GlobalFeedPresenter extends MvpPresenter<GlobalFeedView> {

  private final PostRepository postRepository;

  GlobalFeedPresenter(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  void loadPostsByCategory(boolean pullToRefresh, String cursor, String eTag, int limit,
      PostSorter sorter, String category) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = postRepository.list(cursor, eTag, limit, sorter, category)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showFeed, this::showError);

    getDisposable().add(d);
  }

  void loadPostsByTag(boolean pullToRefresh, String cursor, String eTag, int limit,
      PostSorter sorter, String tag) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = postRepository.list(cursor, eTag, limit, sorter, tag)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showFeed, this::showError);

    getDisposable().add(d);
  }

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  void deletePost(String postId) {
    Disposable d =
        postRepository.delete(postId).observeOn(AndroidSchedulers.mainThread()).subscribe();

    getDisposable().add(d);
  }

  void vote(String postId, int direction) {
    Disposable d = postRepository.vote(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> postRepository.get(postId)
            .observeOn(AndroidSchedulers.mainThread(), true)
            .subscribe(post -> getView().onPostUpdated(post)), this::showError);

    getDisposable().add(d);
  }

  private void showFeed(Response<List<PostRealm>> response) {
    getView().onLoaded(response);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}