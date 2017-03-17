package com.yoloo.android.feature.search;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

class ChildSearchPresenter extends MvpPresenter<ChildSearchView> {

  private final TagRepository tagRepository;
  private final UserRepository userRepository;

  private String userCursor;
  private String tagCursor;

  ChildSearchPresenter(TagRepository tagRepository, UserRepository userRepository) {
    this.userRepository = userRepository;
    this.tagRepository = tagRepository;
  }

  void loadRecentTags() {
    Disposable d = tagRepository.listRecentTags()
        .delay(250, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showRecentTags);

    getDisposable().add(d);
  }

  void searchTags(String name, boolean resetCursor) {
    if (resetCursor) {
      tagCursor = null;
    }

    Disposable d = tagRepository.listTags2(name, tagCursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showTags, Timber::e);

    getDisposable().add(d);
  }

  void loadRecentUsers() {
    Disposable d = userRepository.listRecentSearchedUsers()
        .delay(250, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showRecentUsers);

    getDisposable().add(d);
  }

  void searchUsers(String query, boolean resetCursor) {
    if (resetCursor) {
      userCursor = null;
    }

    Disposable d = userRepository.searchUser(query, userCursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showUsers, Timber::e);

    getDisposable().add(d);
  }

  void follow(String userId, int direction) {
    Disposable d = userRepository.follow(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showRecentTags(List<TagRealm> tags) {
    getView().onRecentTagsLoaded(tags);
  }

  private void showTags(Response<List<TagRealm>> response) {
    tagCursor = response.getCursor();

    getView().onTagsLoaded(response.getData());
  }

  private void showRecentUsers(List<AccountRealm> accounts) {
    getView().onRecentUsersLoaded(accounts);
  }

  private void showUsers(Response<List<AccountRealm>> response) {
    userCursor = response.getCursor();
    getView().onUsersLoaded(response.getData());
  }
}
