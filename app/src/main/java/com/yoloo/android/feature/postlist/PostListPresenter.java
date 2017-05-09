package com.yoloo.android.feature.postlist;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.PostSorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

/**
 * The type Post list presenter.
 */
class PostListPresenter extends MvpPresenter<PostListView> {

  private final PostRepository postRepository;
  private final UserRepository userRepository;

  private String cursor;

  /**
   * Instantiates a new Post list presenter.
   *
   * @param postRepository the post repository
   * @param userRepository the user repository
   */
  PostListPresenter(PostRepository postRepository, UserRepository userRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
  }

  /**
   * Load posts by group.
   *
   * @param pullToRefresh the pull to refresh
   * @param groupId the group id
   * @param sorter the sorter
   * @param limit the limit
   */
  void loadPostsByGroup(boolean pullToRefresh, @Nonnull String groupId, @Nonnull PostSorter sorter,
      int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByGroup(groupId, sorter, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load more posts by group.
   *
   * @param groupId the group id
   * @param sorter the sorter
   * @param limit the limit
   */
  void loadMorePostsByGroup(@Nonnull String groupId, @Nonnull PostSorter sorter, int limit) {
    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByGroup(groupId, sorter, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load posts by tag.
   *
   * @param pullToRefresh the pull to refresh
   * @param tagName the tag name
   * @param sorter the sorter
   * @param limit the limit
   */
  void loadPostsByTag(boolean pullToRefresh, @Nonnull String tagName, @Nonnull PostSorter sorter,
      int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByTags(tagName, sorter, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load more posts by tag.
   *
   * @param tagName the tag name
   * @param sorter the sorter
   * @param limit the limit
   */
  void loadMorePostsByTag(@Nonnull String tagName, @Nonnull PostSorter sorter, int limit) {
    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByTags(tagName, sorter, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load posts by user.
   *
   * @param pullToRefresh the pull to refresh
   * @param userId the user id
   * @param limit the limit
   */
  void loadPostsByUser(boolean pullToRefresh, @Nonnull String userId, int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByUser(userId, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load more posts by user.
   *
   * @param userId the user id
   * @param limit the limit
   */
  void loadMorePostsByUser(@Nonnull String userId, int limit) {
    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByUser(userId, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load posts by bounty.
   *
   * @param pullToRefresh the pull to refresh
   * @param limit the limit
   */
  void loadPostsByBounty(boolean pullToRefresh, int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<Response<List<PostRealm>>> postsObservable =
        postRepository.listByBounty(cursor, limit).observeOn(AndroidSchedulers.mainThread());

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load more posts by bounty.
   *
   * @param limit the limit
   */
  void loadMorePostsByBounty(int limit) {
    Observable<Response<List<PostRealm>>> postsObservable =
        postRepository.listByBounty(cursor, limit).observeOn(AndroidSchedulers.mainThread());

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load posts by bookmarked.
   *
   * @param pullToRefresh the pull to refresh
   * @param limit the limit
   */
  void loadPostsByBookmarked(boolean pullToRefresh, int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByBookmarked(cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load more posts by bookmarked.
   *
   * @param limit the limit
   */
  void loadMorePostsByBookmarked(int limit) {
    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByBookmarked(cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load posts by post sorter.
   *
   * @param pullToRefresh the pull to refresh
   * @param postSorter the post sorter
   * @param limit the limit
   */
  void loadPostsByPostSorter(boolean pullToRefresh, PostSorter postSorter, int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByPostSorter(postSorter, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

    getDisposable().add(d);
  }

  /**
   * Load more posts by post sorter.
   *
   * @param postSorter the post sorter
   * @param limit the limit
   */
  void loadMorePostsByPostSorter(PostSorter postSorter, int limit) {
    Observable<Response<List<PostRealm>>> postsObservable = postRepository
        .listByPostSorter(postSorter, cursor, limit)
        .observeOn(AndroidSchedulers.mainThread(), true);

    Disposable d = Observable
        .zip(getUserObservable(), postsObservable, Pair::create)
        .subscribe(pair -> showData(pair), this::showError);

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
   * Vote post.
   *
   * @param postId the post id
   * @param direction the direction
   */
  void votePost(@Nonnull String postId, int direction) {
    Disposable d = postRepository
        .votePost(postId, direction)
        .andThen(postRepository.getPost(postId))
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(post -> {
          if (post.isPresent()) {
            getView().onPostUpdated(post.get());
          }
        }, this::showError);

    getDisposable().add(d);
  }

  /**
   * Bookmark post.
   *
   * @param postId the post id
   */
  void bookmarkPost(@Nonnull String postId) {
    Disposable d = postRepository
        .bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  /**
   * Un bookmark post.
   *
   * @param postId the post id
   */
  void unBookmarkPost(@Nonnull String postId) {
    Disposable d = postRepository
        .unBookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  private void showData(Pair<AccountRealm, Response<List<PostRealm>>> pair) {
    if (pair.second.getData().isEmpty()) {
      getView().onEmpty();
    } else {
      getView().onAccountLoaded(pair.first);

      cursor = pair.second.getCursor();
      getView().onLoaded(new ArrayList<>(mapPostsToFeedItems(pair.second.getData())));
      getView().showContent();
    }
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private Observable<AccountRealm> getUserObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }

  private List<FeedItem<PostRealm>> mapPostsToFeedItems(List<PostRealm> data) {
    return Stream.of(data).map(post -> {
      if (post.isTextPost()) {
        return new TextPostFeedItem(post);
      } else if (post.isRichPost()) {
        return new RichPostFeedItem(post);
      } else if (post.isBlogPost()) {
        return new BlogFeedItem(post);
      }

      throw new IllegalArgumentException("postType is not supported.");
    }).toList();
  }
}
