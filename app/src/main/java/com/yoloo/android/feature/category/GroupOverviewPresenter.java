package com.yoloo.android.feature.category;

import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class GroupOverviewPresenter extends MvpPresenter<GroupOverviewView> {

  private final GroupRepository groupRepository;

  GroupOverviewPresenter(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  void loadCategories() {
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
        .subscribe(groups -> getView().onGroupsLoaded(groups),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
