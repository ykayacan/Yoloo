package com.yoloo.android.feature.explore;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import com.annimon.stream.Stream;
import com.yoloo.android.data.feed.FeedItem;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.feature.explore.data.ButtonItem;
import com.yoloo.android.feature.explore.data.GroupItem;
import com.yoloo.android.feature.explore.data.RecentMediaListItem;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.NetworkUtil;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

class ExplorePresenter extends MvpPresenter<ExploreView> {

  private final PostRepository postRepository;
  private final GroupRepository groupRepository;

  private final ArrayMap<String, FeedItem<?>> items = new ArrayMap<>(20);

  ExplorePresenter(PostRepository postRepository, GroupRepository groupRepository) {
    this.postRepository = postRepository;
    this.groupRepository = groupRepository;
  }

  @Override public void onAttachView(ExploreView view) {
    super.onAttachView(view);
    loadExploreScreen();
  }

  private void loadExploreScreen() {
    Disposable d = Observable
        .zip(
            getRecentMediaPostsObservable(),
            getGroupsObservable(),
            Pair::create)
        .retry(1, NetworkUtil::isKnownException)
        .map(pair -> {
          List<FeedItem<?>> items = new ArrayList<>(3);
          items.add(pair.first);
          items.add(new ButtonItem());
          items.addAll(pair.second);
          return items;
        })
        .subscribe(items -> {
          Stream.of(items).forEach(feedItem -> this.items.put(feedItem.getId(), feedItem));
          getView().onDataLoaded(items);
        }, Timber::e);

    getDisposable().add(d);
  }

  void subscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .subscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> updateItem(new GroupItem(group)), Timber::e);

    getDisposable().add(d);
  }

  void unsubscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .unsubscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(group -> updateItem(new GroupItem(group)), Timber::e);

    getDisposable().add(d);
  }

  void updateItem(@NonNull FeedItem<?> item) {
    items.put(item.getId(), item);
    getView().onDataLoaded(items.values());
  }

  private Observable<RecentMediaListItem> getRecentMediaPostsObservable() {
    return postRepository
        .listByMediaPosts(null, 8)
        .observeOn(AndroidSchedulers.mainThread())
        .map(response -> new RecentMediaListItem(response.getData()));
  }

  private Observable<List<GroupItem>> getGroupsObservable() {
    return groupRepository
        .listGroups(null, 100)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .flatMap(response -> Observable.fromIterable(response.getData()))
        .distinct()
        .map(GroupItem::new)
        .toList()
        .toObservable();
  }
}
