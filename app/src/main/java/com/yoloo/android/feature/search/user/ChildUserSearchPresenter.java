package com.yoloo.android.feature.search.user;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import timber.log.Timber;

class ChildUserSearchPresenter extends MvpPresenter<ChildUserSearchView> {

  private final UserRepository userRepository;

  private String cursor;

  /**
   * Instantiates a new Child search presenter.
   *
   * @param userRepository the user repository
   */
  ChildUserSearchPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Load recent users.
   */
  void loadRecentUsers() {
    Disposable d = userRepository
        .listRecentSearchedUsers()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showRecentUsers, Timber::e);

    getDisposable().add(d);
  }

  /**
   * Search users.
   *
   * @param query the query
   * @param resetCursor the reset cursor
   */
  void searchUsers(String query, boolean resetCursor) {
    if (resetCursor) {
      cursor = null;
    }

    Disposable d = userRepository
        .searchUser(query, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showUsers, Timber::e);

    getDisposable().add(d);
  }

  private void showRecentUsers(List<AccountRealm> accounts) {
    getView().onRecentUsersLoaded(accounts);
  }

  private void showUsers(Response<List<AccountRealm>> response) {
    cursor = response.getCursor();
    getView().onUsersLoaded(response.getData());
  }
}
