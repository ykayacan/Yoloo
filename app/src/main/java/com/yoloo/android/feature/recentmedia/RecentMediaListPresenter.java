package com.yoloo.android.feature.recentmedia;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.feed.BlogPostFeedItem;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.feed.RichPostFeedItem;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

class RecentMediaListPresenter extends MvpPresenter<RecentMediaListView> {

  private final PostRepository postRepository;

  private String cursor;

  RecentMediaListPresenter(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  void loadRecentMedias(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }

    Disposable d = getRecentMediaPostsObservable()
        .subscribe(items -> getView().onLoaded(new ArrayList<>(items)),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadMoreRecentMedias() {
    Disposable d = getRecentMediaPostsObservable()
        .subscribe(items -> getView().onMoreDataLoaded(new ArrayList<>(items)),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private Single<List<FeedItem<PostRealm>>> getRecentMediaPostsObservable() {
    return postRepository
        .listByMediaPosts(cursor, 30)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(response -> cursor = response.getCursor())
        .map(Response::getData)
        .flatMap(Observable::fromIterable)
        .map(post -> {
          if (post.isRichPost()) {
            return new RichPostFeedItem(post);
          } else if (post.isBlogPost()) {
            return new BlogPostFeedItem(post);
          }

          throw new IllegalArgumentException("post type is not valid");
        })
        .toList();
  }
}
