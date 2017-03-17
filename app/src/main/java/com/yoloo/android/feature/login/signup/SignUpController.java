package com.yoloo.android.feature.login.signup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.bluelinelabs.conductor.RouterTransaction;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.notification.NotificationRepository;
import com.yoloo.android.data.repository.notification.datasource.NotificationDiskDataSource;
import com.yoloo.android.data.repository.notification.datasource.NotificationRemoteDataSource;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.feed.home.FeedHomeController;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.FormUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.LocaleUtil;

import java.net.SocketTimeoutException;
import java.util.ArrayList;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;

public class SignUpController extends MvpController<SignUpView, SignUpPresenter>
    implements SignUpView {

  private static final String KEY_CATEGORY_IDS = "CATEGORY_IDS";

  @BindView(R.id.et_login_fullname) EditText etFullName;
  @BindView(R.id.et_login_email) AutoCompleteTextView etEmail;
  @BindView(R.id.et_login_password) EditText etPassword;

  @BindString(R.string.label_loading) String loadingString;
  @BindString(R.string.error_field_required) String errorFieldRequiredString;
  @BindString(R.string.error_invalid_email) String errorInvalidEmail;
  @BindString(R.string.error_invalid_password) String errorInvalidPassword;
  @BindString(R.string.error_email_already_taken) String errorEmailAlreadyTakenString;
  @BindString(R.string.error_server_down) String errorServerDownString;
  @BindString(R.string.error_already_registered) String errorAlreadyRegisteredString;

  private ArrayList<String> categoryIds;

  private ProgressDialog progressDialog;

  public SignUpController(@NonNull Bundle args) {
    super(args);
    categoryIds = args.getStringArrayList(KEY_CATEGORY_IDS);
  }

  public static SignUpController create(ArrayList<String> topicIds) {
    final Bundle bundle = new BundleBuilder()
        .putStringArrayList(KEY_CATEGORY_IDS, topicIds)
        .build();

    return new SignUpController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_sign_up, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
  }

  @Override public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @NonNull @Override public SignUpPresenter createPresenter() {
    return new SignUpPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        NotificationRepository.getInstance(
            NotificationRemoteDataSource.getInstance(),
            NotificationDiskDataSource.getInstance()
        ));
  }

  @OnClick(R.id.btn_login_ready) void signUp() {
    etFullName.setError(null);
    etEmail.setError(null);
    etPassword.setError(null);

    String fullname = etFullName.getText().toString();
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
    if (TextUtils.isEmpty(password)) {
      etPassword.setError(errorFieldRequiredString);
      focusView = etPassword;
      cancel = true;
    } else if (!FormUtil.isPasswordValid(password)) {
      etPassword.setError(errorInvalidPassword);
      focusView = etPassword;
      cancel = true;
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus();
    } else {
      KeyboardUtil.hideKeyboard(etPassword);

      String locale = LocaleUtil.getCurrentLocale(getActivity()).getISO3Country();

      getPresenter().signUp(fullname, email, password, categoryIds, locale);
    }
  }

  @OnFocusChange(R.id.et_login_email) void onEmailFocusChanged(boolean hasFocus) {
    if (hasFocus) {
      FormUtil.populateEmail(getActivity(), etEmail);
    }
  }

  @OnEditorAction(R.id.et_login_password) boolean onEditorAction(int actionId) {
    if (actionId == R.id.sign_up || actionId == EditorInfo.IME_NULL) {
      signUp();
      return true;
    }
    return false;
  }

  @Override public void onUsernameUnAvailable() {

  }

  @Override public void onSignedUp() {
    getParentController().getRouter().setRoot(RouterTransaction.with(FeedHomeController.create()));
  }

  @Override public void onError(Throwable t) {
    if (t instanceof SocketTimeoutException) {
      Snackbar.make(getView(), errorServerDownString, Snackbar.LENGTH_LONG).show();
    }

    if (t.getMessage().contains("400")) {
      Snackbar.make(getView(), errorEmailAlreadyTakenString, Snackbar.LENGTH_LONG).show();
    }

    if (t.getMessage().contains("409")) {
      Snackbar.make(getView(), errorAlreadyRegisteredString, Snackbar.LENGTH_LONG).show();
    }

    // Backend didn't processed, sign out account.
    AuthUI.getInstance().signOut(getActivity());
  }

  @Override public void onShowLoading() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getActivity());
      progressDialog.setMessage(loadingString);
      progressDialog.setIndeterminate(true);
    }

    progressDialog.show();
  }

  @Override public void onHideLoading() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }
}
