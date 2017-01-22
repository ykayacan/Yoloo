package com.yoloo.android.feature.login.signup;

import com.google.firebase.auth.FirebaseAuth;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.List;
import timber.log.Timber;

public class SignUpPresenter extends MvpPresenter<SignUpView> {

  private final UserRepository userRepository;

  public SignUpPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  public void signUp(String email, String password, List<String> categoryIds, String locale) {
    getView().onShowLoading();

    processFirebase(email, password, convertTopicIdsToString(categoryIds), locale);
  }

  private void processFirebase(String email, String password, String categoryIds, String locale) {
    FirebaseAuth.getInstance()
        .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
          Timber.d("createUserWithEmail:onComplete %s", task.isSuccessful());

          // If sign in fails, display a message to the user. If sign in succeeds
          // the auth state listener will be notified and logic to handle the
          // signed in user can be handled in the listener.
          if (!task.isSuccessful()) {
            getView().onHideLoading();
            getView().onError(task.getException());
          } else {
            registerUserOnServer(email, password, categoryIds, locale);
          }
        });
  }

  private void registerUserOnServer(String email, String password, String categoryIds,
      String locale) {
    AccountRealm newAccount = new AccountRealm().setMe(true)
        .setEmail(email)
        .setPassword(password)
        .setLocale(locale)
        .setCategoryIds(categoryIds);

    Disposable d = userRepository.add(newAccount)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> {
          getView().onHideLoading();
          getView().onSignedUp();
        }, throwable -> {
          getView().onHideLoading();
          getView().onError(throwable);
        });

    getDisposable().add(d);
  }

  private String convertTopicIdsToString(List<String> topicIds) {
    return Observable.fromIterable(topicIds)
        .reduce((s, s2) -> s + "," + s2)
        .map(s -> s.substring(0, s.length() - 1))
        .blockingGet();
  }
}
