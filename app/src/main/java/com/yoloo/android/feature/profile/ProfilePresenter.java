package com.yoloo.android.feature.profile;

import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ProfilePresenter extends MvpPresenter<ProfileView> {

  private final UserRepository userRepository;
  private final PostRepository postRepository;

  public ProfilePresenter(UserRepository userRepository, PostRepository postRepository) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
  }

  public void loadUserProfile(String userId) {
    /*Disposable d = Observable.zip(userRepository.get(userId).toObservable(),
        postRepository.listByUser(userId, null, null, 20), Pair::ofCategory)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          Timber.d("Account: %s", pair.first);
          getView().onProfileLoaded(pair.first);
          getView().onPostsLoaded(pair.second);
        });*/

    Disposable d = userRepository.get(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> getView().onProfileLoaded(account),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private void loadMe() {
    Disposable d = userRepository.getMe()
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(
            account -> getView().onProfileLoaded(account),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  public void follow(String userId, int direction) {
    Disposable d = userRepository.follow(userId, direction)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(Throwable::printStackTrace)
        .subscribe();

    getDisposable().add(d);
  }
}
