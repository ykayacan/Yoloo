package com.yoloo.android.feature.editor.selectgroup;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.GroupSorter;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import javax.annotation.Nonnull;

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
        .flatMapObservable(
            id -> Observable.mergeDelayError(groupRepository.listSubscribedGroups(id),
                getAllGroupsObservable()))
        .filter(groupRealms -> !groupRealms.isEmpty())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(groups -> getView().onLoaded(groups), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void loadSubscribedGroups() {
    Disposable d = userRepository
        .getLocalMe()
        .map(AccountRealm::getId)
        .flatMapObservable(groupRepository::listSubscribedGroups)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(groups -> getView().onLoaded(groups), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void searchGroups(@Nonnull String query) {
    Disposable d = groupRepository
        .searchGroups(query)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(groups -> {
          if (groups.isEmpty()) {
            getView().onEmpty();
          } else {
            getView().onLoaded(groups);
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private Observable<List<GroupRealm>> getAllGroupsObservable() {
    return groupRepository.listGroups(GroupSorter.DEFAULT, null, 20).map(Response::getData);
  }
}
