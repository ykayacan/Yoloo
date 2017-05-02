package com.yoloo.android.feature.auth.signup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bumptech.glide.Glide;
import com.fastaccess.datetimepicker.DatePickerFragmentDialog;
import com.fastaccess.datetimepicker.DateTimeBuilder;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.mikelau.countrypickerx.CountryPickerDialog;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.notification.NotificationRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.feature.auth.IdpResponse;
import com.yoloo.android.feature.base.BaseActivity;
import com.yoloo.android.feature.feed.FeedController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.FormUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.LocaleUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import io.reactivex.subjects.PublishSubject;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class SignUpController extends MvpController<SignUpView, SignUpPresenter>
    implements SignUpView {

  private static final String KEY_TRAVELER_TYPE_IDS = "TRAVELER_TYPE_IDS";
  private static final String KEY_IDP_RESPONSE = "IDP_RESPONSE";

  private static final String DEFAULT_AVATAR_URL =
      "https://storage.googleapis.com/yoloo-151719.appspot.com/system-default/"
          + "empty_user_avatar.webp";

  private final PublishSubject<String> usernameSubject = PublishSubject.create();

  @BindView(R.id.iv_auth_avatar) ImageView ivAvatar;
  @BindView(R.id.et_auth_fullname) EditText etFullName;
  @BindView(R.id.et_auth_username) EditText etUsername;
  @BindView(R.id.et_auth_email) EditText etEmail;
  @BindView(R.id.et_auth_password) EditText etPassword;
  @BindView(R.id.et_auth_birthday) EditText etBirthDate;
  @BindView(R.id.et_auth_select_country) EditText etSelectCountry;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_field_required) String errorFieldRequiredString;
  @BindString(R.string.error_invalid_email) String errorInvalidEmail;
  @BindString(R.string.error_invalid_password) String errorInvalidPassword;
  @BindString(R.string.error_email_already_taken) String errorEmailAlreadyTakenString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;
  @BindString(R.string.error_field_username_unavailable) String errorUsernameAlreadyTaken;
  @BindString(R.string.hint_birthday) String birthdateString;

  @BindString(R.string.hint_sign_up_enter_username) String hintEnterUsernameString;
  @BindString(R.string.hint_sign_up_enter_password) String hintEnterPasswordString;

  private ArrayList<String> travelerTypeIds;
  private IdpResponse idpResponse;

  private ProgressDialog progressDialog;

  private CountryPickerDialog countryPicker;

  private long birthDate = 0L;
  private String countryCode;

  public SignUpController() {
  }

  public SignUpController(@NonNull Bundle args) {
    super(args);
  }

  public static SignUpController create(@NonNull ArrayList<String> travelerTypeIds,
      @NonNull IdpResponse idpResponse) {
    final Bundle bundle = new BundleBuilder()
        .putStringArrayList(KEY_TRAVELER_TYPE_IDS, travelerTypeIds)
        .putParcelable(KEY_IDP_RESPONSE, idpResponse)
        .build();

    return new SignUpController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_sign_up, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();

    travelerTypeIds = getArgs().getStringArrayList(KEY_TRAVELER_TYPE_IDS);
    idpResponse = getArgs().getParcelable(KEY_IDP_RESPONSE);

    handleIdpResponse();

    usernameSubject
        .filter(username -> !TextUtils.isEmpty(username))
        .debounce(400, TimeUnit.MILLISECONDS)
        .subscribe(username -> getPresenter().checkUsername(username));
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    ViewUtils.hideStatusBar(view);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    ViewUtils.clearHideStatusBar(view);
    if (countryPicker.isShowing()) {
      countryPicker.dismiss();
    }
  }

  private void handleIdpResponse() {
    switch (idpResponse.getProviderType()) {
      case AuthUI.FACEBOOK_PROVIDER:
        handleFacebookProvider(idpResponse);
        break;
      case AuthUI.GOOGLE_PROVIDER:
        handleGoogleProvider(idpResponse);
        break;
      case AuthUI.EMAIL_PROVIDER:
        handleEmailProvider();
        break;
    }
  }

  private void handleEmailProvider() {
    etFullName.setEnabled(true);
    etEmail.setEnabled(true);
    etPassword.setEnabled(true);

    Glide
        .with(getActivity())
        .load(DEFAULT_AVATAR_URL)
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivAvatar);
  }

  private void handleFacebookProvider(IdpResponse idpResponse) {
    etFullName.setEnabled(false);
    etEmail.setEnabled(false);
    etPassword.setEnabled(false);
    etPassword.setText("Password");

    etUsername.setHint(hintEnterUsernameString);

    final String avatarUrl = TextUtils.isEmpty(idpResponse.getPictureUrl())
        ? DEFAULT_AVATAR_URL
        : idpResponse.getPictureUrl();

    Glide
        .with(getActivity())
        .load(avatarUrl)
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivAvatar);

    etFullName.setText(idpResponse.getName());
    etEmail.setText(idpResponse.getEmail());
  }

  private void handleGoogleProvider(IdpResponse idpResponse) {
    etFullName.setEnabled(false);
    etEmail.setEnabled(false);
    etPassword.setEnabled(false);
    etPassword.setText("Password");

    final String avatarUrl = TextUtils.isEmpty(idpResponse.getPictureUrl())
        ? DEFAULT_AVATAR_URL
        : idpResponse.getPictureUrl();

    Glide
        .with(getActivity())
        .load(avatarUrl)
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivAvatar);

    etFullName.setText(idpResponse.getName());
    etEmail.setText(idpResponse.getEmail());
  }

  @OnClick(R.id.et_auth_birthday)
  void setBirthDate(EditText editText) {
    ((BaseActivity) getActivity()).setOnDatePickListener(date -> {
      birthDate = date;

      Date birthDate = new Date(date);
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
      String formattedDate = sdf.format(birthDate);
      editText.setText(formattedDate);
    });

    DatePickerFragmentDialog
        .newInstance(DateTimeBuilder.newInstance().withMaxDate(new Date().getTime()))
        .show(((FragmentActivity) getActivity()).getSupportFragmentManager(),
            "DatePickerFragmentDialog");
  }

  @OnClick(R.id.et_auth_select_country)
  void selectCountry(EditText editText) {
    countryPicker = new CountryPickerDialog(getActivity(), (country, flagResId) -> {
      countryCode = country.getIsoCode();
      editText.setText(country.getCountryName(getActivity()));
    }, false, 0);
    countryPicker.show();
  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @NonNull
  @Override
  public SignUpPresenter createPresenter() {
    return new SignUpPresenter(UserRepositoryProvider.getRepository(),
        NotificationRepositoryProvider.getRepository());
  }

  @OnClick(R.id.btn_auth_sign_up)
  void signUp() {
    etFullName.setError(null);
    etEmail.setError(null);
    etPassword.setError(null);

    String fullname = etFullName.getText().toString();
    String username = etUsername.getText().toString();
    String email = etEmail.getText().toString();
    String password = etPassword.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid full name.
    if (TextUtils.isEmpty(fullname)) {
      etFullName.setError(errorFieldRequiredString);
      focusView = etFullName;
      cancel = true;
    }

    // Check for a valid username.
    if (TextUtils.isEmpty(username)) {
      etUsername.setError(errorFieldRequiredString);
      focusView = etUsername;
      cancel = true;
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      etEmail.setError(errorFieldRequiredString);
      focusView = etEmail;
      cancel = true;
    } else if (!FormUtil.isEmailAddress(email)) {
      etEmail.setError(errorInvalidEmail);
      focusView = etEmail;
      cancel = true;
    }

    // Check for a valid password, if the user entered one.
    if (etPassword.isEnabled()) {
      if (TextUtils.isEmpty(password)) {
        etPassword.setError(errorFieldRequiredString);
        focusView = etPassword;
        cancel = true;
      } else if (!FormUtil.isPasswordValid(password)) {
        etPassword.setError(errorInvalidPassword);
        focusView = etPassword;
        cancel = true;
      }
    }

    if (birthDate == 0L) {
      etBirthDate.setError("Select birthday");
      focusView = etBirthDate;
      cancel = true;
    }

    if (TextUtils.isEmpty(countryCode)) {
      etSelectCountry.setError("Select your country");
      focusView = etSelectCountry;
      cancel = true;
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus();
    } else {
      KeyboardUtil.hideKeyboard(getView());

      String locale = LocaleUtil.getCurrentLocale(getActivity()).getISO3Country();

      if (TextUtils.isEmpty(idpResponse.getEmail())) {
        idpResponse = new IdpResponse(idpResponse.getProviderType(), etEmail.getText().toString(),
            idpResponse.getIdpToken(), idpResponse.getName(), idpResponse.getPictureUrl());
      }

      if (idpResponse.getProviderType().equals(AuthUI.FACEBOOK_PROVIDER) || idpResponse
          .getProviderType()
          .equals(AuthUI.GOOGLE_PROVIDER)) {
        getPresenter().signUpWithProvider(idpResponse, username, new Date(birthDate), countryCode,
            locale, travelerTypeIds);
      } else if (idpResponse.getProviderType().equals(AuthUI.EMAIL_PROVIDER)) {
        getPresenter().signUpWithPassword(idpResponse, username, password, new Date(birthDate),
            countryCode, locale, travelerTypeIds);
      }
    }
  }

  @OnEditorAction(R.id.et_auth_password)
  boolean onEditorAction(int actionId) {
    if (actionId == R.id.sign_up || actionId == EditorInfo.IME_NULL) {
      signUp();
      return true;
    }
    return false;
  }

  @OnTextChanged(R.id.et_auth_username)
  void checkUsername(CharSequence text) {
    usernameSubject.onNext(text.toString());
  }

  @Override
  public void onCheckUsername(boolean available) {
    String errorText = available ? null : errorUsernameAlreadyTaken;
    etUsername.setError(errorText);
  }

  @Override
  public void onSignedUp() {
    getRouter().setRoot(RouterTransaction.with(FeedController.create()));
  }

  @Override
  public void onError(Throwable t) {
    // TODO: 18.04.2017 Check error messages
    Timber.d(t);

    if (t instanceof SocketTimeoutException) {
      Snackbar.make(getView(), errorServerDownString, Snackbar.LENGTH_LONG).show();
    }

    if (t.getMessage().contains("400") || t instanceof FirebaseAuthUserCollisionException) {
      Snackbar.make(getView(), errorEmailAlreadyTakenString, Snackbar.LENGTH_LONG).show();
    }

    if (t.getMessage().contains("409")) {
      Toast.makeText(getActivity(), errorAlreadyRegisteredString, Toast.LENGTH_SHORT).show();
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

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
  }
}
