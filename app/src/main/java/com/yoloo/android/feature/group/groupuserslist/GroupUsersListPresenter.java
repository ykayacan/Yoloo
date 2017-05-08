package com.yoloo.android.feature.group.groupuserslist;

import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;

class GroupUsersListPresenter extends MvpPresenter<GroupUsersListView> {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  private String cursor;

  GroupUsersListPresenter(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  void loadUsers(@Nonnull String groupId) {
    Disposable d = groupRepository
        .listGroupUsers(groupId, cursor, 40)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
          cursor = response.getCursor();

          if (response.getData().isEmpty()) {
            getView().onEmpty();
          } else {
            getView().onLoaded(response.getData());
          }
        });

    getDisposable().add(d);
  }

  void follow(@Nonnull String userId) {
    Disposable d = userRepository
        .relationship(userId, 1)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onFollowedSuccessfully(),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void unfollow(@Nonnull String userId) {
    Disposable d = userRepository
        .relationship(userId, -1)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onUnfollowedSuccessfully(),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
