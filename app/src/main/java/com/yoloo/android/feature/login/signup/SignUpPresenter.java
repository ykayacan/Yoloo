package com.yoloo.android.feature.login.signup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class SignUpPresenter extends MvpPresenter<SignUpView> {

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  SignUpPresenter(UserRepository userRepository, NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
  }

  @Override public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  void signUp(String fullname, String email, String password, List<String> categoryIds,
      String locale) {
    getView().onShowLoading();

    final String categoryIdsAsString = Stream.of(categoryIds).collect(Collectors.joining(","));

    processFirebase(fullname, email, password, categoryIdsAsString, locale);
  }

  private void processFirebase(String fullname, String email, String password, String categoryIds,
      String locale) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
          Timber.d("createUserWithEmail:onComplete %s", task.isSuccessful());

          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          if (task.isSuccessful()) {
            registerUser(fullname, categoryIds, locale);
          } else {
            getView().onHideLoading();
            getView().onError(task.getException());
          }
        });
  }

  private void registerUser(String fullname, String categoryIds, String locale) {
    AccountRealm newAccount = new AccountRealm()
        .setMe(true)
        .setRealname(fullname)
        .setGender("MALE")
        .setLocale(locale)
        .setCategoryIds(categoryIds);

    Disposable d = userRepository.registerUser(newAccount)
        .observeOn(AndroidSchedulers.mainThread())
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
