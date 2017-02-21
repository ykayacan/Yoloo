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
import timber.log.Timber;

public class ChildSearchPresenter extends MvpPresenter<ChildSearchView> {

  private final TagRepository tagRepository;
  private final UserRepository userRepository;

  public ChildSearchPresenter(TagRepository tagRepository, UserRepository userRepository) {
    this.userRepository = userRepository;
    this.tagRepository = tagRepository;
  }

  public void loadRecentTags() {
    Disposable d = tagRepository.listRecentTags()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showRecentTags);

    getDisposable().add(d);
  }

  public void loadTags(String name, String cursor) {
    Disposable d = tagRepository.listTags2(name, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showTags, Timber::e);

    getDisposable().add(d);
  }

  public void loadRecentUsers() {
    Disposable d = userRepository.listRecentSearchedUsers()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showRecentUsers);

    getDisposable().add(d);
  }

  public void loadUsers(String name, String cursor) {
    Disposable d = userRepository.searchUser(name, cursor, 20)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showUsers, Timber::e);

    getDisposable().add(d);
  }

  public void follow(String userId, int direction) {
    Disposable d = userRepository.follow(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();

    getDisposable().add(d);
  }

  private void showRecentTags(List<TagRealm> tags) {
    getView().onRecentTagsLoaded(tags);
  }

  private void showTags(Response<List<TagRealm>> response) {
    getView().onTagsLoaded(response);
  }

  private void showRecentUsers(List<AccountRealm> accounts) {
    getView().onRecentUsersLoaded(accounts);
  }

  private void showUsers(Response<List<AccountRealm>> response) {
    getView().onUsersLoaded(response);
  }
}
