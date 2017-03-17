package com.yoloo.android.feature.profile.profileedit;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.VersionUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;

public class ProfileEditController extends MvpController<ProfileEditView, ProfileEditPresenter>
    implements ProfileEditView {

  @BindView(R.id.toolbar_profile_edit) Toolbar toolbar;
  @BindView(R.id.iv_profile_edit_save) ImageView ivSaveIcon;
  @BindView(R.id.iv_profile_edit_avatar) ImageView ivAvatar;
  @BindView(R.id.til_profile_edit_realname) TextInputLayout tilRealName;
  @BindView(R.id.til_profile_edit_username) TextInputLayout tilUsername;
  @BindView(R.id.til_profile_edit_email) TextInputLayout tilEmail;
  @BindView(R.id.til_profile_edit_password) TextInputLayout tilPassword;
  @BindView(R.id.spinner_profile_edit_gender) Spinner spinnerGender;
  @BindView(R.id.til_profile_edit_website) TextInputLayout tilWebsite;
  @BindView(R.id.til_profile_edit_bio) TextInputLayout tilBio;

  @BindString(R.string.error_profile_edit_empty_realname) String realNameEmptyErrorString;
  @BindString(R.string.error_profile_edit_empty_username) String usernameEmptyErrorString;
  @BindString(R.string.error_profile_edit_empty_email) String emailEmptyErrorString;

  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private AccountRealm me;

  private boolean hasErrors;

  public ProfileEditController() {
    setHasOptionsMenu(true);
  }

  public static ProfileEditController create() {
    return new ProfileEditController();
  }

  @Override protected View inflateView(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_profile_edit, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);

    setupToolbar();

    ControllerUtil.preventDefaultBackPressAction(view, this::showDiscardDialog);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ViewUtils.setStatusBarColor(getActivity(), Color.BLACK);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      showDiscardDialog();
      return false;
    }
    return super.onOptionsItemSelected(item);
  }

  private void validateFields() {

  }

  private void showDiscardDialog() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.action_catalog_discard_draft)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> getRouter().handleBack())
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  @Override public void onAccountUpdated(AccountRealm account) {
    Timber.d("Account: %s", account);
    getRouter().handleBack();
  }

  @Override public void onLoading(boolean pullToRefresh) {

  }

  @Override public void onLoaded(AccountRealm account) {
    me = account;
    setProfile(account);
  }

  @Override public void onError(Throwable e) {

  }

  @Override public void onEmpty() {

  }

  @NonNull @Override public ProfileEditPresenter createPresenter() {
    return new ProfileEditPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()
        ));
  }

  @OnTextChanged(R.id.et_profile_edit_realname) void listenRealName(CharSequence text) {
    tilRealName.setError(TextUtils.isEmpty(text) ? realNameEmptyErrorString : null);
    hasErrors = TextUtils.isEmpty(text);
  }

  @OnTextChanged(R.id.et_profile_edit_username) void listenUsername(CharSequence text) {
    tilUsername.setError(TextUtils.isEmpty(text) ? usernameEmptyErrorString : null);
    hasErrors = TextUtils.isEmpty(text);
  }

  @OnTextChanged(R.id.et_profile_edit_email) void listenEmail(CharSequence text) {
    tilEmail.setError(TextUtils.isEmpty(text) ? emailEmptyErrorString : null);
    hasErrors = TextUtils.isEmpty(text);
  }

  @OnClick({
      R.id.iv_profile_edit_avatar,
      R.id.tv_profile_edit_change_photo
  }) void changeAvatar() {

  }

  @OnClick(R.id.iv_profile_edit_save) void saveProfile() {
    Timber.d("Has errors: %s", hasErrors);
  }

  private void setProfile(AccountRealm account) {
    Glide.with(getActivity())
        .load(account.getAvatarUrl().replace("s96-c", "s80-c-rw"))
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivAvatar);

    if (VersionUtil.hasL()) {
      ivAvatar.setTransitionName(getResources().getString(R.string.transition_avatar));
    }

    tilRealName.getEditText().setText(account.getRealname());
    tilUsername.getEditText().setText(account.getUsername());
    tilEmail.getEditText().setText(account.getEmail());

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
      tilPassword.setEnabled(user.getProviderId().equals(AuthUI.EMAIL_PROVIDER));
    }

    ArrayAdapter adapter = (ArrayAdapter) spinnerGender.getAdapter();
    final String gender = account.getGender();
    // Convert gender string to spinner version
    // E.g. female -> Female
    final String spinnerGenderText = gender.substring(0, 1).toUpperCase() + gender.substring(1);
    final int spinnerPosition = adapter.getPosition(spinnerGenderText);
    spinnerGender.setSelection(spinnerPosition);

    tilWebsite.getEditText().setText(account.getWebsiteUrl());
    tilBio.getEditText().setText(account.getBio());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPostToBeginning back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_profile_edit_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeAsUpIndicator(
          AppCompatResources.getDrawable(getActivity(), R.drawable.ic_close_white_24dp));
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}
