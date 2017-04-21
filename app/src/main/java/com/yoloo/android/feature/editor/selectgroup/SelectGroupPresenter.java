package com.yoloo.android.feature.editor.selectgroup;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class SelectGroupPresenter extends MvpPresenter<SelectGroupView> {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  private String cursor;

  SelectGroupPresenter(UserRepository userRepository, GroupRepository groupRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
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
}
