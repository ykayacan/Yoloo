package com.yoloo.android.feature.feed.userfeed;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;

class UserFeedPresenter extends MvpPresenter<UserFeedView> {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;

  /**
   * Instantiates a new User feed presenter.
   *
   * @param postRepository the post data repository
   * @param categoryRepository the topic data repository
   */
  UserFeedPresenter(PostRepository postRepository, CategoryRepository categoryRepository) {
    this.postRepository = postRepository;
    this.categoryRepository = categoryRepository;
  }

  /**
   * Load feed.
   *
   * @param pullToRefresh the pull to refresh
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   */
  void loadFeed(boolean pullToRefresh, String cursor, String eTag, int limit) {
    if (pullToRefresh) {
      getView().onLoading(pullToRefresh);
    }

    Disposable d = postRepository.listFeed(cursor, eTag, limit)
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
    Disposable d = postRepository.delete(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  /**
   * Load trending topics.
   */
  void loadTrendingCategories() {
    Disposable d = categoryRepository.list(7, CategorySorter.TRENDING)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showTrendingCategories, this::showError);

    getDisposable().add(d);
  }

  void vote(String postId, int direction) {
    Disposable d = postRepository.vote(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showTrendingCategories(List<CategoryRealm> topicRealms) {
    getView().onTrendingCategoriesLoaded(topicRealms);
  }

  private void showFeed(Response<List<PostRealm>> response) {
    getView().onLoaded(response);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}