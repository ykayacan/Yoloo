package com.yoloo.android.feature.auth.signup;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
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

  /**
   * Sign up with provider.
   *
   * @param response the response
   * @param username the username
   * @param birthday the birthday
   * @param countryCode the country code
   * @param langCode the lang code
   * @param travelerTypeIds the traveler type ids
   */
  void signUpWithProvider(@Nonnull IdpResponse response, @Nonnull String username,
      @Nonnull Date birthday, @Nonnull String countryCode, @Nonnull String langCode,
      @Nonnull List<String> travelerTypeIds) {
    getView().onShowLoading();

    AuthCredential credential = getAuthCredential(response, response.getProviderType());

    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
      Timber.d("signInWithCredential:onComplete: %s", task.isSuccessful());

      // If sign in fails, display a message to the user. If sign in succeeds
      // the auth state listener will be notified and logic to handle the
      // signed in user can be handled in the listener.
      if (task.isSuccessful()) {
        AccountRealm newAccount = new AccountRealm()
            .setMe(true)
            .setRealname(response.getName())
            .setUsername(username)
            .setAvatarUrl(response.getPictureUrl())
            .setEmail(response.getEmail())
            .setBirthdate(birthday)
            .setCountry(new CountryRealm(countryCode))
            .setLangCode(langCode)
            .setTravelerTypeIds(travelerTypeIds);

        registerUserOnServer(newAccount);
      } else {
        getView().onHideLoading();
        getView().onError(task.getException());
      }
    });
  }

  /**
   * Sign up with password.
   *
   * @param response the response
   * @param username the username
   * @param password the password
   * @param birthday the birthday
   * @param countryCode the country code
   * @param langCode the lang code
   * @param travelerTypeIds the traveler type ids
   */
  void signUpWithPassword(@Nonnull IdpResponse response, @Nonnull String username,
      @Nonnull String password, @Nonnull Date birthday, @Nonnull String countryCode,
      @Nonnull String langCode, @Nonnull List<String> travelerTypeIds) {
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
            AccountRealm newAccount = new AccountRealm()
                .setMe(true)
                .setRealname(response.getName())
                .setUsername(username)
                .setPassword(password)
                .setAvatarUrl(response.getPictureUrl())
                .setEmail(response.getEmail())
                .setBirthdate(birthday)
                .setCountry(new CountryRealm(countryCode))
                .setLangCode(langCode)
                .setTravelerTypeIds(travelerTypeIds);

            registerUserOnServer(newAccount);
          } else {
            getView().onHideLoading();
            getView().onError(task.getException());
          }
        });
  }

  private void registerUserOnServer(AccountRealm newAccount) {
    Disposable d = userRepository
        .registerUser(newAccount)
        .flatMapCompletable(account -> registerFcmToken())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> {
          getView().onHideLoading();
          getView().onSignedUp();
        }, throwable -> {
          FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Timber.d("User account deleted.");
            }
          });

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
