package com.yoloo.android.feature.feed.userfeed;

import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

class UserFeedPresenter extends MvpPresenter<UserFeedView> {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  UserFeedPresenter(
      PostRepository postRepository,
      CategoryRepository categoryRepository,
      NotificationRepository notificationRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.categoryRepository = categoryRepository;
    this.notificationRepository = notificationRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(UserFeedView view) {
    super.onAttachView(view);
    Timber.d("onAttachView()");
    loadTrendingCategories();
    loadFeed(false, null, null, 50);
  }

  void loadFeed(boolean pullToRefresh, String cursor, String eTag, int limit) {
    Timber.d("loadFeed()");
    getView().onLoading(pullToRefresh);

    Disposable d = Observable
        .zip(
            postRepository.listByFeed(cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          Timber.d("pair: %s", pair.first.getData().size());
          getView().onAccountLoaded(pair.second);

          if (pair.first.getData() == null) {
            getView().onEmpty();
          } else {
            getView().onLoaded(pair.first);
          }

          getView().showContent();
        }, this::showError);

    getDisposable().add(d);
  }

  private void loadTrendingCategories() {
    Disposable d = categoryRepository.listCategories(7, CategorySorter.TRENDING)
        .delay(250, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(categories -> getView().onTrendingCategoriesLoaded(categories), this::showError);

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

  void registerFcmToken(String token) {
    Disposable d = notificationRepository.registerFcmToken(new FcmRealm(token))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}