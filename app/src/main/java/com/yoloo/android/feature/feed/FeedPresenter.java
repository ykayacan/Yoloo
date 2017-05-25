package com.yoloo.android.feature.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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
import com.yoloo.android.feature.editor.job.SendPostJob;
import com.yoloo.android.feature.feed.common.annotation.FeedAction;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import com.yoloo.android.util.NetworkUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parceler.Parcels;
import timber.log.Timber;

/**
 * The type Feed presenter.
 */
class FeedPresenter extends MvpPresenter<FeedView> {

  private final PostRepository postRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  private List<FeedItem<?>> items = Collections.emptyList();

  private String cursor;

  private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      PostRealm post = Parcels.unwrap(intent.getParcelableExtra(SendPostJob.KEY_ADD_POST));
      items.addAll(items.size() >= 3 ? 3 : 2, mapPostsToFeedItems(Collections.singletonList(post)));
      getView().onLoaded(new FeedState(false, false, items));
    }
  };

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

  @Override public void onAttachView(FeedView view) {
    super.onAttachView(view);
    LocalBroadcastManager
        .getInstance(view.getAppContext())
        .registerReceiver(newPostReceiver, new IntentFilter(SendPostJob.SEND_POST_EVENT));
  }

  @Override public void onDetachView() {
    LocalBroadcastManager.getInstance(getView().getAppContext())
        .unregisterReceiver(newPostReceiver);
    super.onDetachView();
  }

  void loadMe() {
    Disposable d = userRepository.getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> getView().onMeLoaded(account),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadFirstPage() {
    Disposable d = Observable.zip(
        getRecommendedGroupsObservable(),
        getTrendingBlogsObservable(),
        getFeedObservable(),
        getNewUsersObservable(),
        Group.Of4::create)
        .doOnNext(group -> {
          this.cursor = group.third.getCursor();
        })
        .retry(1, NetworkUtil::isKnownException)
        .map(this::mapGroupsToFeedItems)
        .map(feedItems -> new FeedState(true, false, feedItems))
        .startWith(new FeedState(false, false, items))
        .doOnNext(state -> this.items = state.getData())
        .subscribe(state -> {
          if (isViewAttached()) {
            getView().onLoaded(state);
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadNextPage() {
    Timber.d("loadNextPage()");
    Disposable d = getFeedObservable()
        .retry(1, NetworkUtil::isKnownException)
        .doOnNext(response -> cursor = response.getCursor())
        .doOnNext(response -> items.addAll(mapPostsToFeedItems(response.getData())))
        .map(response -> new FeedState(true, false, items))
        .startWith(new FeedState(true, true, items))
        .subscribe(state -> {
          if (isViewAttached()) {
            getView().onLoaded(state);
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadPullToRefresh() {
    this.cursor = null;

    Disposable d = getFeedObservable()
        .retry(1, NetworkUtil::isKnownException)
        .doOnNext(response -> {
          this.items.subList(3, this.items.size()).clear();
          this.items.addAll(mapPostsToFeedItems(response.getData()));
          this.cursor = response.getCursor();
        })
        .map(response -> new FeedState(true, false, this.items))
        .subscribe(state -> {
          if (isViewAttached()) {
            getView().onLoaded(state);
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  /**
   * Delete post.
   *
   * @param post the post
   */
  void deletePost(PostRealm post) {
    Disposable d = postRepository
        .deletePost(post.getId())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
          this.items.removeAll(mapPostsToFeedItems(Collections.singletonList(post)));
          getView().onLoaded(new FeedState(true, false, this.items));
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
        .subscribe(updated -> {
          FeedItem<?> item = mapToPostFeedItem(updated);
          if (item != null) {
            updateFeedItem(item);
          }
        }, Timber::e);

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
        .subscribe(post -> {
          FeedItem<?> item = mapToPostFeedItem(post);
          if (item != null) {
            updateFeedItem(item);
          }
        }, Timber::e);

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
        .subscribe(post -> {
          FeedItem<?> item = mapToPostFeedItem(post);
          if (item != null) {
            updateFeedItem(item);
          }
        }, Timber::e);

    getDisposable().add(d);
  }

  /**
   * Follow.
   *
   * @param account the account
   * @param direction the direction
   */
  void follow(AccountRealm account, int direction) {
    Disposable d = userRepository
        .relationship(account.getId(), direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
          for (FeedItem<?> item : items) {
            if (item instanceof NewUserListFeedItem) {
              ((NewUserListFeedItem) item).getItem().remove(account);
              getView().onLoaded(new FeedState(true, false, items));
              break;
            }
          }
        }, Timber::e);

    getDisposable().add(d);
  }

  void updateEvent(@FeedAction int action, Object payload) {
    if (payload instanceof PostRealm) {
      final FeedItem<?> item = mapToPostFeedItem((PostRealm) payload);
      if (item != null) {
        if (action == FeedAction.UPDATE) {
          updateFeedItem(item);
        } else if (action == FeedAction.DELETE) {
          items.remove(item);
          getView().onLoaded(new FeedState(true, false, items));
        }
      }
    }
  }

  private Observable<List<PostRealm>> getTrendingBlogsObservable() {
    return postRepository
        .listByTrendingBlogPosts(null, 4)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Response<List<PostRealm>>> getFeedObservable() {
    return postRepository.listByFeed(cursor, 20).observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<List<GroupRealm>> getRecommendedGroupsObservable() {
    return groupRepository
        .listGroups(GroupSorter.DEFAULT, null, 7)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<List<AccountRealm>> getNewUsersObservable() {
    return NetworkUtil.isNetworkAvailable(YolooApp.getAppContext()) ?
        userRepository
            .listNewUsers(null, 8)
            .map(Response::getData)
            .observeOn(AndroidSchedulers.mainThread()) : Observable.just(Collections.emptyList());
  }

  @NonNull
  private List<FeedItem<?>> mapGroupsToFeedItems(Group.Of4<List<GroupRealm>, List<PostRealm>,
      Response<List<PostRealm>>, List<AccountRealm>> group) {
    List<FeedItem<?>> items = new ArrayList<>();

    if (!group.first.isEmpty()) {
      items.add(new RecommendedGroupListFeedItem(group.first));
    }

    if (!group.second.isEmpty()) {
      items.add(new TrendingBlogListFeedItem(group.second));
    }

    items.add(new BountyButtonFeedItem(
        YolooApp.getAppContext().getResources().getString(R.string.action_feed_bounty_questions)));

    if (!group.fourth.isEmpty()) {
      items.add(new NewUserListFeedItem(group.fourth));

      for (AccountRealm account : group.fourth) {
        items.add(new NewUserWelcomeFeedItem(account));
      }
    }

    items.addAll(mapPostsToFeedItems(group.third.getData()));

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

  private void updateFeedItem(@NonNull FeedItem<?> item) {
    final int size = items.size();
    for (int i = 0; i < size; i++) {
      if (items.get(i).id().equals(item.id())) {
        items.set(i, item);
        getView().onLoaded(new FeedState(true, false, items));
        break;
      }
    }
  }

  @Nullable private FeedItem<?> mapToPostFeedItem(PostRealm post) {
    if (post.isTextPost()) {
      return new TextPostFeedItem(post);
    } else if (post.isRichPost()) {
      return new RichPostFeedItem(post);
    } else if (post.isBlogPost()) {
      return new BlogPostFeedItem(post);
    }

    return null;
  }

  static class FeedState {
    final boolean pullToRefresh;
    final boolean loadingMore;
    final List<FeedItem<?>> data;

    FeedState(boolean pullToRefresh, boolean loadingMore, List<FeedItem<?>> data) {
      this.pullToRefresh = pullToRefresh;
      this.loadingMore = loadingMore;
      this.data = data;
    }

    boolean isPullToRefresh() {
      return pullToRefresh;
    }

    boolean isLoadingMore() {
      return loadingMore;
    }

    List<FeedItem<?>> getData() {
      return data;
    }
  }
}
