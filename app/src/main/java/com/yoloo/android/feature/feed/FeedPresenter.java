package com.yoloo.android.feature.feed;

import android.support.annotation.NonNull;
import com.annimon.stream.Stream;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.BountyButtonFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.NewUserListFeedItem;
import com.yoloo.android.data.feed.NewUserWelcomeFeedItem;
import com.yoloo.android.data.feed.RecommendedGroupListFeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.feed.TextPostFeedItem;
import com.yoloo.android.data.feed.TrendingBlogListFeedItem;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import com.yoloo.android.util.NetworkUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import timber.log.Timber;

/**
 * The type Feed presenter.
 */
class FeedPresenter extends MvpPresenter<FeedView> {

  private final PostRepository postRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  private String cursor;

  /**
   * Instantiates a new Feed presenter.
   *
   * @param postRepository the post repository
   * @param groupRepository the group repository
   * @param userRepository the user repository
   */
  FeedPresenter(PostRepository postRepository, GroupRepository groupRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  /**
   * Load feed.
   *
   * @param pullToRefresh the pull to refresh
   */
  void loadFeed(boolean pullToRefresh) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Disposable d = Observable.zip(
        getMeObservable(),
        getRecommendedGroupsObservable(),
        getTrendingBlogsObservable(),
        getFeedObservable(),
        getNewUsersObservable(),
        Group.Of5::create)
        .doOnNext(group -> {
          getView().onMeLoaded(group.first);
          cursor = group.fourth.getCursor();
        })
        .retry(2, throwable -> throwable instanceof SocketTimeoutException)
        .map(this::mapGroupsToFeedItems)
        .subscribe(feedItems -> {
          getView().onLoaded(feedItems);
          getView().showContent();
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Load more posts.
   */
  void loadMorePosts() {
    Timber.e("loadMorePosts()");
    Disposable d = getFeedObservable()
        .retry(2, throwable -> throwable instanceof SocketTimeoutException)
        .subscribe(response -> {
          cursor = response.getCursor();

          getView().onMoreLoaded(new ArrayList<>(mapPostsToFeedItems(response.getData())));
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Delete post.
   *
   * @param postId the post id
   */
  void deletePost(String postId) {
    Disposable d = postRepository
        .deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Bookmark post.
   *
   * @param postId the post id
   */
  void bookmarkPost(String postId) {
    Disposable d = postRepository
        .bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  /**
   * Un bookmark post.
   *
   * @param postId the post id
   */
  void unBookmarkPost(String postId) {
    Disposable d = postRepository
        .unBookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  /**
   * Vote post.
   *
   * @param postId the post id
   * @param direction the direction
   */
  void votePost(String postId, int direction) {
    Disposable d = postRepository
        .votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(post -> getView().onPostUpdated(post), Timber::e);

    getDisposable().add(d);
  }

  /**
   * Follow.
   *
   * @param userId the user id
   * @param direction the direction
   */
  void follow(String userId, int direction) {
    Disposable d = userRepository
        .relationship(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  private Observable<List<PostRealm>> getTrendingBlogsObservable() {
    return postRepository
        .listByTrendingBlogPosts(null, 4)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread(), true);
  }

  private Observable<Response<List<PostRealm>>> getFeedObservable() {
    return postRepository.listByFeed(cursor, 20).observeOn(AndroidSchedulers.mainThread(), true);
  }

  private Observable<List<GroupRealm>> getRecommendedGroupsObservable() {
    return groupRepository
        .listGroups(GroupSorter.DEFAULT, null, 7)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread(), true);
  }

  private Observable<List<AccountRealm>> getNewUsersObservable() {
    return NetworkUtil.isNetworkAvailable(YolooApp.getAppContext()) ?
        userRepository
            .listNewUsers(null, 8)
            .map(Response::getData)
            .observeOn(AndroidSchedulers.mainThread()) : Observable.just(Collections.emptyList());
  }

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }

  @NonNull
  private List<FeedItem<?>> mapGroupsToFeedItems(
      Group.Of5<AccountRealm, List<GroupRealm>, List<PostRealm>, Response<List<PostRealm>>,
          List<AccountRealm>> group) {
    List<FeedItem<?>> items = new ArrayList<>();

    if (!group.second.isEmpty()) {
      items.add(new RecommendedGroupListFeedItem(group.second));
    }

    if (!group.third.isEmpty()) {
      items.add(new TrendingBlogListFeedItem(group.third));
    }

    items.add(new BountyButtonFeedItem(
        YolooApp.getAppContext().getResources().getString(R.string.action_feed_bounty_questions)));

    if (!group.fifth.isEmpty()) {
      items.add(new NewUserListFeedItem(group.fifth));

      for (AccountRealm account : group.fifth) {
        items.add(group.third.isEmpty() ? 2 : 3, new NewUserWelcomeFeedItem(account));
      }
    }

    items.addAll(mapPostsToFeedItems(group.fourth.getData()));

    return items;
  }

  private List<FeedItem<PostRealm>> mapPostsToFeedItems(List<PostRealm> data) {
    return Stream.of(data).map(post -> {
      if (post.isTextPost()) {
        return new TextPostFeedItem(post);
      } else if (post.isRichPost()) {
        return new RichPostFeedItem(post);
      } else if (post.isBlogPost()) {
        return new BlogPostFeedItem(post);
      }

      return null;
    }).toList();
  }
}
