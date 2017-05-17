package com.yoloo.android.feature.auth.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.rafakob.floatingedittext.FloatingEditText;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.provider.FacebookProvider;
import com.yoloo.android.feature.auth.provider.GoogleProvider;
import com.yoloo.android.feature.auth.provider.IdpProvider;
import com.yoloo.android.feature.chat.NewChatListenerService;
import com.yoloo.android.feature.feed.FeedController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.FormUtil;
import com.yoloo.android.util.KeyboardUtil;
import java.net.SocketTimeoutException;
import timber.log.Timber;

import static com.yoloo.android.util.ServiceUtil.isMyServiceRunning;

public class SignInController extends MvpController<SignInView, SignInPresenter>
    implements SignInView, IdpProvider.IdpCallback {

  @BindView(R.id.fet_login_email) FloatingEditText fetEmail;
  @BindView(R.id.fet_login_password) FloatingEditText fetPassword;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindString(R.string.error_google_play_services) String errorGooglePlayServicesString;
  @BindString(R.string.error_auth_failed) String errorAuthFailedString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_field_required) String errorFieldRequiredString;
  @BindString(R.string.error_invalid_email) String errorInvalidEmail;
  @BindString(R.string.error_invalid_password) String errorInvalidPassword;

  private ProgressDialog progressDialog;

  private IdpProvider idpProvider;

  public static SignInController create() {
    return new SignInController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_sign_in, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();

    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (idpProvider != null) {
      idpProvider.setAuthenticationCallback(null);
    }
  }

  @NonNull
  @Override
  public SignInPresenter createPresenter() {
    return new SignInPresenter(UserRepositoryProvider.getRepository(),
        NotificationRepositoryProvider.getRepository());
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    idpProvider.onActivityResult(requestCode, resultCode, data);
  }

  @OnClick(R.id.btn_google_sign_in)
  void signInWithGoogle() {
    idpProvider = getIdpProvider(AuthUI.GOOGLE_PROVIDER);
    idpProvider.setAuthenticationCallback(this);

    idpProvider.startLogin(this);
  }

  @OnClick(R.id.btn_facebook_sign_in)
  void signInWithFacebook() {
    idpProvider = getIdpProvider(AuthUI.FACEBOOK_PROVIDER);
    idpProvider.setAuthenticationCallback(this);

    idpProvider.startLogin(this);
  }

  @OnClick(R.id.btn_login_ready)
  void signInWithEmail() {
    fetEmail.setError(null);
    fetPassword.setError(null);

    final String email = fetEmail.getText();
    final String password = fetPassword.getText();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      fetEmail.setError(errorFieldRequiredString);
      focusView = fetEmail;
      cancel = true;
    } else if (!FormUtil.isEmailAddress(email)) {
      fetEmail.setError(errorInvalidEmail);
      focusView = fetEmail;
      cancel = true;
    }

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      fetPassword.setError(errorFieldRequiredString);
      focusView = fetPassword;
      cancel = true;
    } else if (!FormUtil.isPasswordValid(password)) {
      fetPassword.setError(errorInvalidPassword);
      focusView = fetPassword;
      cancel = true;
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus();
    } else {
      KeyboardUtil.hideKeyboard(getView());

      getPresenter().signIn(email, password);
    }
  }

  @Override
  public void onSignedIn() {
    if (!isMyServiceRunning(NewChatListenerService.class, getActivity())) {
      getActivity().startService(new Intent(getActivity(), NewChatListenerService.class));
    }

    getRouter().setRoot(RouterTransaction.with(FeedController.create()));
  }

  @Override
  public void onError(Throwable t) {
    Timber.e(t);
    Snackbar.make(getView(), errorAuthFailedString, Snackbar.LENGTH_SHORT).show();

    if (t instanceof SocketTimeoutException) {
      Snackbar.make(getView(), errorServerDownString, Snackbar.LENGTH_SHORT).show();
    }

    // Backend didn't processed, sign out account.
    AuthUI.getInstance().signOut((FragmentActivity) getActivity());
  }

  @Override
  public void onShowLoading() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getActivity());
      progressDialog.setMessage(loadingString);
      progressDialog.setIndeterminate(true);
    }

    progressDialog.show();
  }

  @Override
  public void onHideLoading() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public void onSuccess(IdpResponse idpResponse) {
    getPresenter().signIn(idpResponse);
  }

  @Override
  public void onFailure(Bundle extra) {
    //Snackbar.make(getView(), extra.getString("error", null), Snackbar.LENGTH_SHORT).show();
  }

  private IdpProvider getIdpProvider(String providerId) {
    final AuthUI.IdpConfig config = new AuthUI.IdpConfig.Builder(providerId).build();

    switch (providerId) {
      case AuthUI.GOOGLE_PROVIDER:
        return new GoogleProvider(this, config);
      case AuthUI.FACEBOOK_PROVIDER:
        config.addScope("user_friends");
        return new FacebookProvider(config);
      default:
        throw new UnsupportedOperationException("Given providerId is not valid!");
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }
}
