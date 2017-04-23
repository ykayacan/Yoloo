package com.yoloo.android.feature.explore;

import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.media.MediaRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Group;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

class ExplorePresenter extends MvpPresenter<ExploreView> {

  private final MediaRepository mediaRepository;
  private final GroupRepository groupRepository;

  ExplorePresenter(MediaRepository mediaRepository, GroupRepository groupRepository) {
    this.mediaRepository = mediaRepository;
    this.groupRepository = groupRepository;
  }

  @Override
  public void onAttachView(ExploreView view) {
    super.onAttachView(view);
    loadExploreScreen();
  }

  private void loadExploreScreen() {
    Observable.zip(getRecentMediasObservable(), getButtonObservable(), getGroupsObservable(),
        Group.Of3::create)
        .subscribe(group -> {
          List<ExploreItem<?>> items = new ArrayList<>();

          items.add(group.first);
          items.add(group.second);
          items.addAll(group.third);

          getView().onDataLoaded(items);
        }, Timber::e);
  }

  void subscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .subscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  void unsubscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .unsubscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, Timber::e);

    getDisposable().add(d);
  }

  private Observable<RecentMediaItem> getRecentMediasObservable() {
    return mediaRepository
        .listRecentMedias(null, 8)
        .observeOn(AndroidSchedulers.mainThread())
        .map(response -> new RecentMediaItem(response.getData()));
  }

  private Observable<ButtonItem> getButtonObservable() {
    return Observable.just(new ButtonItem());
  }

  private Observable<List<GroupItem>> getGroupsObservable() {
    return groupRepository
        .listGroups(GroupSorter.DEFAULT, null, 100)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .flatMap(response -> Observable.fromIterable(response.getData()))
        .map(GroupItem::new)
        .toList()
        .toObservable();
  }
}
