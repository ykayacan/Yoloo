package com.yoloo.android.feature.auth.signin;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yoloo.android.data.db.FcmRealm;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.provider.FacebookProvider;
import com.yoloo.android.feature.auth.provider.GoogleProvider;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import javax.annotation.Nonnull;
import timber.log.Timber;

class SignInPresenter extends MvpPresenter<SignInView> {

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  SignInPresenter(UserRepository userRepository, NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
  }

  @Override
  public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  void signIn(@Nonnull IdpResponse response) {
    final String providerType = response.getProviderType();
    if (providerType.equals(AuthUI.GOOGLE_PROVIDER)) {
      processFirebase(GoogleProvider.createAuthCredential(response));
    } else if (providerType.equals(AuthUI.FACEBOOK_PROVIDER)) {
      processFirebase(FacebookProvider.createAuthCredential(response));
    }
  }

  void signIn(@Nonnull String email, @Nonnull String password) {
    processFirebase(EmailAuthProvider.getCredential(email, password));
  }

  private void processFirebase(AuthCredential credential) {
    getView().onShowLoading();

    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
      Timber.d("signInWithCredential:onComplete %s", task.isSuccessful());

      // If sign in fails, display a message to the user. If sign in succeeds
      // the auth state listener will be notified and logic to handle the
      // signed in user can be handled in the listener.
      if (task.isSuccessful()) {
        loadUser();
      } else {
        getView().onHideLoading();
        getView().onError(task.getException());
      }
    });
  }

  private void loadUser() {
    Disposable d = notificationRepository
        .registerFcmToken(new FcmRealm(FirebaseInstanceId.getInstance().getToken()))
        .andThen(userRepository.getMe())
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(account -> {
          getView().onHideLoading();
          getView().onSignedIn();
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
}
