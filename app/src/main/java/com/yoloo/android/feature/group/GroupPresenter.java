package com.yoloo.android.feature.group;

import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import com.yoloo.android.util.Pair;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class GroupPresenter extends MvpPresenter<GroupView> {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  GroupPresenter(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  void loadGroupInfoAndPosts(@Nonnull String groupId) {
    Observable<GroupRealm> groupRealmObservable =
        groupRepository.getGroup(groupId).observeOn(AndroidSchedulers.mainThread()).toObservable();

    Disposable d = Observable
        .zip(getUserObservable(), groupRealmObservable, Pair::create)
        .subscribe(this::showData, this::showError);

    getDisposable().add(d);
  }

  void subscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .subscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, this::showError);

    getDisposable().add(d);
  }

  void unsubscribe(@Nonnull String groupId) {
    Disposable d = groupRepository
        .unsubscribe(groupId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
        }, this::showError);

    getDisposable().add(d);
  }

  private void showData(Pair<AccountRealm, GroupRealm> pair) {
    getView().onAccountLoaded(pair.first);
    getView().onLoaded(pair.second);
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }

  private Observable<AccountRealm> getUserObservable() {
    return userRepository.getLocalMe().toObservable().observeOn(AndroidSchedulers.mainThread());
  }
}
