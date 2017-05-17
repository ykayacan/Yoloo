package com.yoloo.android.feature.groupgridoverview;

import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class GroupGridOverviewPresenter extends MvpPresenter<GroupGridOverviewView> {

  private final GroupRepository groupRepository;

  GroupGridOverviewPresenter(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  void loadGroups() {
    Disposable d = groupRepository
        .listGroups(GroupSorter.DEFAULT, null, 100)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(response -> getView().onGroupsLoaded(response.getData()),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadSubscribedGroups(@Nonnull String userId) {
    Disposable d = groupRepository
        .listSubscribedGroups(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(groups -> {
              if (isViewAttached()) {
                getView().onGroupsLoaded(groups);
              }
            },
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
