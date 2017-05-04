package com.yoloo.android.feature.auth.signupinit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.auth.provider.FacebookProvider;
import com.yoloo.android.feature.auth.provider.GoogleProvider;
import com.yoloo.android.feature.auth.provider.IdpProvider;
import com.yoloo.android.feature.auth.signupprepared.SignUpPreparedController;
import com.yoloo.android.feature.auth.signupprogress.SignUpProgressController;
import com.yoloo.android.feature.base.BaseController;

public class SignUpInitController extends BaseController implements IdpProvider.IdpCallback {

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindString(R.string.error_auth_failed) String errorAuthFailedString;
  @BindString(R.string.error_google_play_services) String errorGooglePlayServicesString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;
  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_unknownhost) String unknownhostString;

  private IdpProvider idpProvider;

  public static SignUpInitController create() {
    return new SignUpInitController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_sign_up_init, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (idpProvider != null) {
      idpProvider.setAuthenticationCallback(null);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    idpProvider.onActivityResult(requestCode, resultCode, data);
  }

  @OnClick(R.id.btn_google_sign_in)
  void signUpWithGoogle() {
    idpProvider = getIdpProvider(AuthUI.GOOGLE_PROVIDER);
    idpProvider.setAuthenticationCallback(this);

    idpProvider.startLogin(this);
  }

  @OnClick(R.id.btn_facebook_sign_in)
  void signUpWithFacebook() {
    idpProvider = getIdpProvider(AuthUI.FACEBOOK_PROVIDER);
    idpProvider.setAuthenticationCallback(this);

    idpProvider.startLogin(this);
  }

  @OnClick(R.id.btn_login_sign_up)
  void signUp() {
    getRouter().pushController(RouterTransaction
        .with(SignUpProgressController.create(R.layout.controller_sign_up_name, null))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override
  public void onSuccess(IdpResponse idpResponse) {
    getRouter().pushController(RouterTransaction
        .with(SignUpPreparedController.create(idpResponse))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
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
