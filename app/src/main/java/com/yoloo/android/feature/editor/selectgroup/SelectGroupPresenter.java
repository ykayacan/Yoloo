package com.yoloo.android.feature.editor.selectgroup;

import com.annimon.stream.Stream;
import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.EpoxyItem;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import timber.log.Timber;

class SelectGroupPresenter extends MvpPresenter<SelectGroupView> {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  SelectGroupPresenter(UserRepository userRepository, GroupRepository groupRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
  }

  void loadGroups() {
    Disposable d = userRepository
        .getLocalMe()
        .map(AccountRealm::getId)
        .flatMapObservable(id -> Observable.merge(groupRepository.listSubscribedGroups(id),
            getAllGroupsObservable()))
        .flatMap(Observable::fromIterable)
        .distinct(GroupRealm::getId)
        .groupBy(GroupRealm::isSubscribed)
        .flatMap(groupedBySubscribe -> groupedBySubscribe
            .toList()
            .map(groups -> {
              boolean subscribe = groupedBySubscribe.getKey();

              List<EpoxyItem<?>> items = new ArrayList<>();
              items.add(!subscribe ? new SubscribedHeader() : new AllHeader());
              items.addAll(Stream.of(groups).map(GroupItem::new).toList());

              return items;
            }).toObservable())
        .concatMap(Observable::fromIterable)
        .toList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(groups -> getView().onLoaded(groups), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void searchGroups(@Nonnull String query) {
    Disposable d = groupRepository
        .searchGroups(query)
        .flatMap(groups -> Observable.fromIterable(groups).map(GroupItem::new))
        .map(item -> {
          List<EpoxyItem<?>> items = new ArrayList<>();
          items.add(item);
          return items;
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(items -> {
          if (items.isEmpty()) {
            getView().onEmpty();
          } else {
            getView().onLoaded(items);
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
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

  private Observable<List<GroupRealm>> getAllGroupsObservable() {
    return groupRepository.listGroups(GroupSorter.DEFAULT, null, 20).map(Response::getData);
  }

  static class SubscribedHeader implements EpoxyItem<Void> {
    @Override public Void getItem() {
      return null;
    }
  }

  static class AllHeader implements EpoxyItem<Void> {
    @Override public Void getItem() {
      return null;
    }
  }

  static class GroupItem implements EpoxyItem<GroupRealm> {

    private final GroupRealm group;

    private GroupItem(GroupRealm group) {
      this.group = group;
    }

    @Override public GroupRealm getItem() {
      return group;
    }
  }
}
