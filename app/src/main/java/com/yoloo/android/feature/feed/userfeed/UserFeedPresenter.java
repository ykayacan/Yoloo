package com.yoloo.android.feature.feed.userfeed;

import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;

class UserFeedPresenter extends MvpPresenter<UserFeedView> {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  UserFeedPresenter(
      PostRepository postRepository,
      CategoryRepository categoryRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(UserFeedView view) {
    super.onAttachView(view);
    loadFeed();
  }

  void loadFeed() {
    getView().onLoading(false);

    Disposable d = Observable
        .zip(
            userRepository.getLocalMe(),
            categoryRepository.listCategories(7, CategorySorter.TRENDING),
            postRepository.listByFeed(null, null, 20),
            Group.Of3::create)
        .delay(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> {
          getView().onAccountLoaded(group.first);
          getView().onTrendingCategoriesLoaded(group.second);
          getView().onLoaded(group.third);

          getView().showContent();
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadPosts(boolean pullToRefresh, String cursor, String eTag, int limit) {
    getView().onLoading(pullToRefresh);

    Disposable d = postRepository.listByFeed(cursor, eTag, limit)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(response -> {
          if (response.getData().isEmpty()) {
            getView().onEmpty();
          } else {
            getView().onLoaded(response);
          }

          getView().showContent();
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void deletePost(String postId) {
    Disposable d = postRepository.deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void bookmarkPost(String postId) {
    Disposable d = postRepository.bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  void votePost(String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }
}