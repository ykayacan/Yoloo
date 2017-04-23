package com.yoloo.android.feature.feed;

import android.support.annotation.NonNull;
import android.util.Pair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.feedtypes.BlogFeedItem;
import com.yoloo.android.data.feedtypes.BountyButtonFeedItem;
import com.yoloo.android.data.feedtypes.FeedItem;
import com.yoloo.android.data.feedtypes.NewUsersFeedItem;
import com.yoloo.android.data.feedtypes.RichQuestionFeedItem;
import com.yoloo.android.data.feedtypes.TextQuestionFeedItem;
import com.yoloo.android.data.feedtypes.TrendingBlogsFeedItem;
import com.yoloo.android.data.feedtypes.TrendingCategoriesFeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import timber.log.Timber;

class FeedHomePresenter extends MvpPresenter<FeedHomeView> {

  private final PostRepository postRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  private String cursor;
  private boolean isFirstLoad = true;
  private int totalPostCount = 0;

  FeedHomePresenter(PostRepository postRepository, GroupRepository groupRepository,
      UserRepository userRepository) {
    this.postRepository = postRepository;
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  @Override
  public void onAttachView(FeedHomeView view) {
    super.onAttachView(view);
    loadFeed();
  }

  private void loadFeed() {
    getView().onLoading(false);

    Disposable d = Observable
        .zip(getMeObservable().doOnNext(accountRealm -> Timber.d("Me: %s", accountRealm)),
            getTrendingCategoriesObservable().doOnNext(
                listOptional -> Timber.d("Trending cats: %s", listOptional.size())),
            getTrendingBlogsObservable().doOnNext(
                listOptional -> Timber.d("Trending blogs: %s", listOptional.size())),
            getBountyButtonObservable().doOnNext(
                objectOptional -> Timber.d("Button: %s", objectOptional.get())),
            getFeedObservable().doOnNext(
                response -> Timber.d("Posts size: %s", response.getData().size())),
            getNewUsersObservable().doOnNext(
                listOptional -> Timber.d("New users: %s", listOptional.size())),
            Group.Of6::create)
        .doOnNext(group -> {
          getView().onMeLoaded(group.first);
          totalPostCount = group.fifth.getData().size();
        })
        .map(this::mapGroupsToFeedItems)
        .doOnComplete(() -> isFirstLoad = false)
        .subscribe(feedItems -> {
          getView().onLoaded(feedItems);
          getView().showContent();
        }, Timber::e);

    getDisposable().add(d);
  }

  void loadPosts(boolean pullToRefresh, int limit) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Disposable d = Observable
        .zip(getFeedObservable(), getNewUsersObservable(), Pair::create)
        .subscribe(pair -> {
          if (pair.first.getData().isEmpty()) {
            getView().onEmpty();
          } else {
            cursor = pair.first.getCursor();
            totalPostCount = pair.first.getData().size();
            getView().onLoaded(mapPostsToFeedItems(pair.first.getData()));
            getView().showContent();
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void deletePost(String postId) {
    Disposable d = postRepository
        .deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void bookmarkPost(String postId) {
    Disposable d = postRepository
        .bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void unBookmarkPost(String postId) {
    Disposable d = postRepository
        .unBookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void votePost(String postId, int direction) {
    Disposable d = postRepository
        .votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private Observable<List<PostRealm>> getTrendingBlogsObservable() {
    return isFirstLoad ? postRepository
        .listByTrendingBlogPosts(null, 4)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread(), true) : Observable.just(Collections.emptyList());
  }

  private Observable<Response<List<PostRealm>>> getFeedObservable() {
    return postRepository.listByFeed(cursor, 20).observeOn(AndroidSchedulers.mainThread(), true);
  }

  private Observable<List<GroupRealm>> getTrendingCategoriesObservable() {
    return isFirstLoad ? groupRepository
        .listGroups(GroupSorter.DEFAULT, null, 7)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread(), true) : Observable.just(Collections.emptyList());
  }

  private Observable<List<AccountRealm>> getNewUsersObservable() {
    return shouldLoadNewcomersComponent() ? userRepository
        .listNewUsers(null, 8)
        .map(Response::getData)
        .observeOn(AndroidSchedulers.mainThread()) : Observable.just(Collections.emptyList());
  }

  private Observable<AccountRealm> getMeObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Optional<Object>> getBountyButtonObservable() {
    return isFirstLoad
        ? Observable.just(Optional.of(new Object()))
        : Observable.just(Optional.empty());
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }

  private boolean shouldLoadNewcomersComponent() {
    return totalPostCount == 0;
  }

  @NonNull
  private List<FeedItem> mapGroupsToFeedItems(
      Group.Of6<AccountRealm, List<GroupRealm>, List<PostRealm>, Optional<Object>, Response<List<PostRealm>>, List<AccountRealm>> group) {
    List<FeedItem> feedItems = new ArrayList<>();

    if (!group.second.isEmpty()) {
      FeedItem item = new TrendingCategoriesFeedItem(group.second);
      feedItems.add(item);
    }

    if (!group.third.isEmpty()) {
      FeedItem item = new TrendingBlogsFeedItem(group.third);
      feedItems.add(item);
    }

    if (group.fourth.isPresent()) {
      FeedItem item = new BountyButtonFeedItem();
      feedItems.add(item);
    }

    cursor = group.fifth.getCursor();
    List<FeedItem> items = mapPostsToFeedItems(group.fifth.getData());
    feedItems.addAll(items);

    if (!group.sixth.isEmpty()) {
      FeedItem item = new NewUsersFeedItem(group.sixth);
      feedItems.add(item);
    }

    return feedItems;
  }

  private List<FeedItem> mapPostsToFeedItems(List<PostRealm> data) {
    return Stream.of(data).map(post -> {
      if (post.getPostType() == PostRealm.TYPE_TEXT) {
        return new TextQuestionFeedItem(post);
      } else if (post.getPostType() == PostRealm.TYPE_RICH) {
        return new RichQuestionFeedItem(post);
      } else if (post.getPostType() == PostRealm.TYPE_BLOG) {
        return new BlogFeedItem(post);
      }

      return null;
    }).toList();
  }
}
