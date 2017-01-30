package com.yoloo.android.feature.feed.userfeed;

import com.yoloo.android.data.model.CategoryRealm;
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
import java.util.List;

public class UserFeedPresenter extends MvpPresenter<UserFeedView> {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  public UserFeedPresenter(
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
    loadTrendingCategories();
    loadFeed(false, null, null, 20);
  }

  /**
   * Load feed.
   *
   * @param pullToRefresh the pull to refresh
   * @param cursor the cursor
   * @param eTag the e tag
   * @param limit the limit
   */
  public void loadFeed(boolean pullToRefresh, String cursor, String eTag, int limit) {
    Disposable d = Observable
        .zip(
            postRepository.listByUserFeed(cursor, eTag, limit),
            userRepository.getLocalMe(),
            Pair::create)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .doOnSubscribe(disposable -> getView().onLoading(pullToRefresh))
        .subscribe(pair -> {
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

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  public void deletePost(String postId) {
    Disposable d = postRepository.delete(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  /**
   * Bookmark post.
   *
   * @param postId the post id
   */
  public void bookmarkPost(String postId) {
    Disposable d = postRepository.bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  /**
   * Vote.
   *
   * @param postId the post id
   * @param direction the direction
   */
  public void votePost(String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  /**
   * Register fcm token.
   *
   * @param token the token
   */
  public void registerFcmToken(String token) {
    Disposable d = notificationRepository.registerFcmToken(new FcmRealm(token))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  public void loadTrendingCategories() {
    Disposable d = categoryRepository.list(7, CategorySorter.TRENDING)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(this::showTrendingCategories, this::showError);

    getDisposable().add(d);
  }

  private void showTrendingCategories(List<CategoryRealm> topicRealms) {
    getView().onTrendingCategoriesLoaded(topicRealms);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}