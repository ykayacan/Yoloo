package com.yoloo.android.feature.feed.home;

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
import com.yoloo.android.data.feedtypes.TravelNewsFeedItem;
import com.yoloo.android.data.feedtypes.TrendingCategoriesFeedItem;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.data.model.NewsRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.news.NewsRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class FeedHomePresenter extends MvpPresenter<FeedHomeView> {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final NewsRepository newsRepository;

  private String cursor;
  private boolean isFirstLoad = true;
  private int totalPostCount = 0;

  FeedHomePresenter(
      PostRepository postRepository,
      CategoryRepository categoryRepository,
      UserRepository userRepository, NewsRepository newsRepository) {
    this.postRepository = postRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
    this.newsRepository = newsRepository;
  }

  @Override public void onAttachView(FeedHomeView view) {
    super.onAttachView(view);
    loadFeed();
  }

  void loadFeed() {
    getView().onLoading(false);

    Disposable d = Observable
        .zip(
            getMeObservable().doOnNext(accountRealm -> Timber.d("Me: %s", accountRealm)),
            getTrendingCategoriesObservable()
                .doOnNext(listOptional -> Timber.d("Trending cats: %s", listOptional.get().size())),
            getTravelNewsObservable()
                .doOnNext(listOptional -> Timber.d("Travel news: %s", listOptional.get().size())),
            getBountyButtonObservable()
                .doOnNext(objectOptional -> Timber.d("Button: %s", objectOptional.get())),
            getFeedObservable()
                .doOnNext(response -> Timber.d("Posts size: %s", response.getData().size())),
            getNewUsersObservable()
                .doOnNext(listOptional -> Timber.d("New users: %s", listOptional.get().size())),
            Group.Of6::create)
        .doOnNext(group -> {
          getView().onAccountLoaded(group.first);
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

    Disposable d = Observable.zip(getFeedObservable(), getNewUsersObservable(), Pair::create)
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
    Disposable d = postRepository.deletePost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void bookmarkPost(String postId) {
    Disposable d = postRepository.bookmarkPost(postId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void votePost(String postId, int direction) {
    Disposable d = postRepository.votePost(postId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private Observable<Optional<List<NewsRealm>>> getTravelNewsObservable() {
    return isFirstLoad
        ? newsRepository.listNews(null, 4).map(Response::getData).map(Optional::of)
        : Observable.just(Optional.empty());
  }

  private Observable<Response<List<PostRealm>>> getFeedObservable() {
    return postRepository.listByFeed(cursor, 20)
        .observeOn(AndroidSchedulers.mainThread(), true);
  }

  private Observable<Optional<List<CategoryRealm>>> getTrendingCategoriesObservable() {
    return isFirstLoad
        ? categoryRepository.listCategories(CategorySorter.DEFAULT, 7)
        .map(Optional::of).observeOn(AndroidSchedulers.mainThread(), true)
        : Observable.just(Optional.empty());
  }

  private Observable<Optional<List<AccountRealm>>> getNewUsersObservable() {
    return shouldLoadNewcomersComponent()
        ? userRepository.listNewUsers(null, 8).map(Response::getData).map(Optional::of)
        .observeOn(AndroidSchedulers.mainThread())
        : Observable.just(Optional.empty());
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

  @NonNull private List<FeedItem> mapGroupsToFeedItems(
      Group.Of6<AccountRealm, Optional<List<CategoryRealm>>,
          Optional<List<NewsRealm>>, Optional<Object>,
          Response<List<PostRealm>>, Optional<List<AccountRealm>>> group) {
    List<FeedItem> feedItems = new ArrayList<>();

    if (group.second.isPresent()) {
      FeedItem item = new TrendingCategoriesFeedItem(group.second.get());
      feedItems.add(item);
    }

    if (group.third.isPresent()) {
      FeedItem item = new TravelNewsFeedItem(group.third.get());
      feedItems.add(item);
    }

    if (group.fourth.isPresent()) {
      FeedItem item = new BountyButtonFeedItem();
      feedItems.add(item);
    }

    cursor = group.fifth.getCursor();
    List<FeedItem> items = mapPostsToFeedItems(group.fifth.getData());
    feedItems.addAll(items);

    if (group.sixth.isPresent()) {
      FeedItem item = new NewUsersFeedItem(group.sixth.get());
      feedItems.add(item);
    }

    return feedItems;
  }

  private List<FeedItem> mapPostsToFeedItems(List<PostRealm> data) {
    return Stream.of(data)
        .map(post -> {
          if (post.getPostType() == PostRealm.POST_TEXT) {
            return new TextQuestionFeedItem(post);
          } else if (post.getPostType() == PostRealm.POST_RICH) {
            return new RichQuestionFeedItem(post);
          } else if (post.getPostType() == PostRealm.POST_BLOG) {
            return new BlogFeedItem(post);
          }

          return null;
        })
        .toList();
  }
}
