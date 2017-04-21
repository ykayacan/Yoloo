package com.yoloo.android.feature.grouplist;

import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class GroupListPresenter extends MvpPresenter<GroupListView> {

  private final GroupRepository groupRepository;

  private String cursor;

  GroupListPresenter(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  void loadGroups(boolean pullToRefresh) {
    getView().onLoading(pullToRefresh);

    shouldResetCursor(pullToRefresh);

    Disposable d = groupRepository
        .listGroups(GroupSorter.DEFAULT, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          cursor = response.getCursor();

          if (response.getData().isEmpty()) {
            getView().onEmpty();
          } else {
            getView().onLoaded(response.getData());
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void subscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .subscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void unsubscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .unsubscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private void shouldResetCursor(boolean pullToRefresh) {
    if (pullToRefresh) {
      cursor = null;
    }
  }
}
