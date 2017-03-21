package com.yoloo.android.feature.login.provider;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.repository.category.CategoryRepository;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.sorter.CategorySorter;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.login.FacebookProvider;
import com.yoloo.android.feature.login.GoogleProvider;
import com.yoloo.android.feature.login.IdpResponse;
import com.yoloo.android.framework.MvpPresenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class ProviderPresenter extends MvpPresenter<ProviderView> {

  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final NotificationRepository notificationRepository;

  ProviderPresenter(UserRepository userRepository, CategoryRepository categoryRepository,
      NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.categoryRepository = categoryRepository;
    this.notificationRepository = notificationRepository;
  }

  @Override public void onAttachView(ProviderView view) {
    super.onAttachView(view);
    loadCategories();
  }

  @Override public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  private void loadCategories() {
    Disposable d = categoryRepository.listCategories(CategorySorter.DEFAULT, Integer.MAX_VALUE)
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(categories -> getView().onCategoriesLoaded(categories),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void signUp(IdpResponse response, String categoryIds, String locale) {
    getView().onShowLoading();

    final String providerType = response.getProviderType();
    if (providerType.equals(AuthUI.GOOGLE_PROVIDER)) {
      processFirebase(GoogleProvider.createAuthCredential(response), categoryIds, locale);
    } else if (providerType.equals(AuthUI.FACEBOOK_PROVIDER)) {
      processFirebase(FacebookProvider.createAuthCredential(response), categoryIds, locale);
    }
  }

  private void processFirebase(AuthCredential credential, String categoryIds, String locale) {
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener(task -> {
          Timber.d("signInWithCredential:onComplete: %s", task.isSuccessful());

          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          if (task.isSuccessful()) {
            registerUser(categoryIds, locale);
          } else {
            getView().onHideLoading();
            getView().onError(task.getException());
          }
        });
  }

  private void registerUser(String categoryIds, String locale) {
    AccountRealm newAccount = new AccountRealm()
        .setMe(true)
        .setRealname("")
        .setGender("MALE")
        .setLocale(locale)
        .setCategoryIds(categoryIds);

    Disposable d = userRepository.registerUser(newAccount)
        .flatMapCompletable(accountRealm -> {
          String fcmToken = FirebaseInstanceId.getInstance().getToken();
          FcmRealm fcm = new FcmRealm();
          fcm.setToken(fcmToken);
          return notificationRepository.registerFcmToken(fcm);
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
          getView().onHideLoading();
          getView().onSignedUp();
        }, throwable -> {
          getView().onHideLoading();
          getView().onError(throwable);
        });

    getDisposable().add(d);
  }
}
