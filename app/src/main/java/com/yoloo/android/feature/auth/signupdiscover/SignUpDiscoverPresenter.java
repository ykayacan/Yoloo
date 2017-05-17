package com.yoloo.android.feature.auth.signupdiscover;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.CountryRealm;
import com.yoloo.android.data.db.FcmRealm;
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
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import timber.log.Timber;

class SignUpDiscoverPresenter extends MvpPresenter<SignUpDiscoverView> {

  private static final String EMPTY_USER_IMAGE =
      "https://storage.googleapis.com/yoloo-151719.appspot.com/system-default/"
          + "empty_user_avatar.webp";

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  SignUpDiscoverPresenter(UserRepository userRepository,
      NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
  }

  @Override
  public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  /**
   * Sign up with provider.
   *
   * @param response the response
   * @param username the username
   * @param countryCode the country code
   * @param langCode the lang code
   * @param travelerTypeIds the traveler type ids
   */
  void signUpWithProvider(@Nonnull IdpResponse response, @Nonnull String username,
      @Nonnull String countryCode, @Nonnull String langCode,
      @Nonnull List<String> travelerTypeIds) {
    Timber.d("signUpWithProvider()");
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
            .setCountry(new CountryRealm(countryCode))
            .setLangCode(langCode)
            .setTravelerTypeIds(travelerTypeIds)
            .setFacebookId(response.getId());

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
   * @param realname the realname
   * @param username the username
   * @param email the email
   * @param password the password
   * @param countryCode the country code
   * @param langCode the lang code
   * @param travelerTypeIds the traveler type ids
   */
  void signUpWithPassword(@Nonnull String realname, @Nonnull String username, @Nonnull String email,
      @Nonnull String password, @Nonnull String countryCode,
      @Nonnull String langCode, @Nonnull List<String> travelerTypeIds) {
    getView().onShowLoading();

    FirebaseAuth
        .getInstance()
        .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
          Timber.d("signInWithPassword:onComplete: %s", task.isSuccessful());

          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          if (task.isSuccessful()) {
            AccountRealm newAccount = new AccountRealm()
                .setMe(true)
                .setRealname(realname)
                .setUsername(username)
                .setPassword(password)
                .setAvatarUrl(EMPTY_USER_IMAGE)
                .setEmail(email)
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
        .flatMapCompletable(ignored -> registerFcmToken())
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
    return notificationRepository.registerFcmToken(
        new FcmRealm(FirebaseInstanceId.getInstance().getToken()));
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
