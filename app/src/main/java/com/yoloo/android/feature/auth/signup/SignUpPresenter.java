package com.yoloo.android.feature.auth.signup;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.FcmRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.provider.FacebookProvider;
import com.yoloo.android.feature.auth.provider.GoogleProvider;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

class SignUpPresenter extends MvpPresenter<SignUpView> {

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  SignUpPresenter(UserRepository userRepository, NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
  }

  @Override
  public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  /**
   * Check username.
   *
   * @param username the username
   */
  void checkUsername(@Nonnull String username) {
    Disposable d = userRepository
        .checkUsername(username.toLowerCase().trim())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(available -> getView().onCheckUsername(available), Timber::e);

    getDisposable().add(d);
  }

  void signUp(@Nonnull IdpResponse response, @Nonnull String username, @Nullable String password,
      @Nonnull Date birthdate, @Nonnull String country, @Nonnull List<String> travelerTypeIds,
      @Nonnull String locale) {
    if (response.getProviderType().equals(AuthUI.EMAIL_PROVIDER)) {
      Timber.d("Password: %s", password);
      signUpWithPassword(response, username, password, birthdate, country, travelerTypeIds, locale);
    } else {
      signUpWithProvider(response, username, birthdate, country, travelerTypeIds, locale);
    }
  }

  private void signUpWithProvider(@Nonnull IdpResponse response, @Nonnull String username,
      @Nonnull Date birthdate, @Nonnull String country, @Nonnull List<String> travelerTypeIds,
      @Nonnull String locale) {
    final String providerType = response.getProviderType();

    AuthCredential credential = getAuthCredential(response, providerType);

    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
      Timber.d("signInWithCredential:onComplete: %s", task.isSuccessful());

      // If sign in fails, display a message to the user. If sign in succeeds
      // the auth state listener will be notified and logic to handle the
      // signed in user can be handled in the listener.
      if (task.isSuccessful()) {
        registerUser(response, username, birthdate, country, travelerTypeIds, locale, null);
      } else {
        getView().onHideLoading();
        getView().onError(task.getException());
      }
    });
  }

  private void signUpWithPassword(@Nonnull IdpResponse response, @Nonnull String username,
      @Nonnull String password, @Nonnull Date birthdate, @Nonnull String country,
      @Nonnull List<String> travelerTypeIds, @Nonnull String locale) {
    getView().onShowLoading();

    FirebaseAuth
        .getInstance()
        .createUserWithEmailAndPassword(response.getEmail(), password)
        .addOnCompleteListener(task -> {
          Timber.d("signInWithPassword:onComplete: %s", task.isSuccessful());

          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          if (task.isSuccessful()) {
            registerUser(response, username, birthdate, country, travelerTypeIds, locale, password);
          } else {
            getView().onHideLoading();
            getView().onError(task.getException());
          }
        });
  }

  private void registerUser(IdpResponse response, String username, Date birthdate, String country,
      List<String> travelerTypeIds, String locale, String password) {
    AccountRealm newAccount = new AccountRealm()
        .setMe(true)
        .setRealname(response.getName())
        .setUsername(username)
        .setAvatarUrl(response.getPictureUrl())
        .setEmail(response.getEmail())
        .setBirthdate(birthdate)
        .setCountry(country)
        .setLocale(locale)
        .setTravelerTypeIds(travelerTypeIds)
        .setPassword(password);

    Disposable d = userRepository
        .registerUser(newAccount)
        .flatMapCompletable(account -> registerFcmToken())
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

  private CompletableSource registerFcmToken() {
    String fcmToken = FirebaseInstanceId.getInstance().getToken();
    FcmRealm fcm = new FcmRealm();
    fcm.setToken(fcmToken);
    return notificationRepository.registerFcmToken(fcm);
  }

  @Nullable
  private AuthCredential getAuthCredential(@Nonnull IdpResponse response, String providerType) {
    AuthCredential credential = null;
    if (providerType.equals(AuthUI.GOOGLE_PROVIDER)) {
      credential = GoogleProvider.createAuthCredential(response);
    } else if (providerType.equals(AuthUI.FACEBOOK_PROVIDER)) {
      credential = FacebookProvider.createAuthCredential(response);
    }
    return credential;
  }
}
