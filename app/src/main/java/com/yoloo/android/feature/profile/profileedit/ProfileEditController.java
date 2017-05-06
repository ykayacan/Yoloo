package com.yoloo.android.feature.profile.profileedit;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.bumptech.glide.Glide;
import com.github.jksiezni.permissive.Permissive;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikelau.countrypickerx.CountryPickerDialog;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;
import com.yalantis.ucrop.UCrop;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.CountryRealm;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.auth.AuthUI;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.FormUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MediaUtil;
import com.yoloo.android.util.TextViewUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.io.File;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class ProfileEditController extends MvpController<ProfileEditView, ProfileEditPresenter>
    implements ProfileEditView {

  private static final int REQUEST_SELECT_MEDIA = 1;
  private static final int REQUEST_CAPTURE_MEDIA = 2;

  @BindView(R.id.toolbar_profile_edit) Toolbar toolbar;
  @BindView(R.id.iv_profile_edit_save) ImageView ivSaveIcon;
  @BindView(R.id.iv_profile_edit_avatar) ImageView ivAvatar;
  @BindView(R.id.til_profile_edit_realname) TextInputLayout tilRealName;
  @BindView(R.id.til_profile_edit_username) TextInputLayout tilUsername;
  @BindView(R.id.til_profile_edit_email) TextInputLayout tilEmail;
  @BindView(R.id.til_profile_edit_password) TextInputLayout tilPassword;
  @BindView(R.id.til_profile_edit_country) TextInputLayout tilCountry;
  @BindView(R.id.spinner_profile_edit_gender) Spinner spinnerGender;
  @BindView(R.id.til_profile_edit_website) TextInputLayout tilWebsite;
  @BindView(R.id.til_profile_edit_bio) TextInputLayout tilBio;

  @BindString(R.string.error_profile_edit_empty_realname) String realNameEmptyErrorString;
  @BindString(R.string.error_profile_edit_empty_username) String usernameEmptyErrorString;
  @BindString(R.string.error_profile_edit_empty_email) String emailEmptyErrorString;
  @BindString(R.string.error_profile_edit_empty_password) String passwordEmptyErrorString;
  @BindString(R.string.error_profile_edit_username_unavailable) String usernameUnavailableString;
  @BindString(R.string.error_profile_edit_invalid_email) String invalidEmailString;
  @BindString(R.string.error_profile_edit_invalid_website) String invalidWebsiteString;
  @BindString(R.string.error_invalid_password) String invalidPasswordString;
  @BindString(R.string.label_loading) String loadingString;

  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(R.color.primary) int primaryColor;

  private PublishSubject<String> usernameSubject = PublishSubject.create();

  private PublishSubject<Boolean> realNameErrorSubject = PublishSubject.create();
  private PublishSubject<Boolean> usernameErrorSubject = PublishSubject.create();
  private PublishSubject<Boolean> emailErrorSubject = PublishSubject.create();
  private PublishSubject<Boolean> passwordErrorSubject = PublishSubject.create();
  private PublishSubject<Boolean> websiteErrorSubject = PublishSubject.create();

  private AccountRealm draft = new AccountRealm();
  private AccountRealm original;

  private Disposable disposable;

  private ProgressDialog progressDialog;

  private CountryPickerDialog countryPicker;

  public ProfileEditController() {
    setHasOptionsMenu(true);
  }

  public static ProfileEditController create() {
    return new ProfileEditController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_profile_edit, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    ViewUtils.setStatusBarColor(getActivity(), primaryDarkColor);

    ControllerUtil.preventDefaultBackPressAction(view, this::showDiscardDialog);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    usernameSubject
        .filter(s -> !s.equals(original.getUsername()))
        .filter(s -> !s.isEmpty())
        .debounce(400, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(username -> getPresenter().checkUsername(username))
        .subscribe();

    // TODO: 19.03.2017 Fix error handling
    disposable = Observable
        .combineLatest(realNameErrorSubject, usernameErrorSubject, emailErrorSubject,
            passwordErrorSubject, websiteErrorSubject,
            (validRealName, validUsername, validEmail, validPassword, validWebsiteUrl) ->
                validRealName
                    && validUsername
                    && validEmail
                    && validPassword
                    && validWebsiteUrl)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(enabled -> {
          Timber.d("Enabled: %s", enabled);
          ivSaveIcon.setImageAlpha(enabled ? 255 : 138);
          ivSaveIcon.setEnabled(enabled);
        });
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    KeyboardUtil.hideKeyboard(view);
    if (countryPicker != null && countryPicker.isShowing()) {
      countryPicker.dismiss();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == REQUEST_SELECT_MEDIA) {
      if (resultCode == Activity.RESULT_OK) {
        handleGalleryResult(data);
      }
    } else if (requestCode == REQUEST_CAPTURE_MEDIA) {
      if (resultCode == Activity.RESULT_OK) {
        handleCameraResult(data);
      }
    } else if (requestCode == UCrop.REQUEST_CROP) {
      if (resultCode == Activity.RESULT_OK) {
        handleCropResult(data);
      } else if (resultCode == UCrop.RESULT_ERROR) {
        handleCropError(data);
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      showDiscardDialog();
      return false;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showDiscardDialog() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.action_catalog_discard_draft)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> getRouter().handleBack())
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  @Override
  public void onAccountUpdated(AccountRealm account) {
    Timber.d("Account: %s", account);
    getRouter().handleBack();
  }

  @Override
  public void onUsernameUnavailable() {
    tilUsername.setError(usernameUnavailableString);
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
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(AccountRealm account) {
    original = account;
    draft
        .setId(account.getId())
        .setMe(account.isMe())
        .setSubscribedGroupIds(account.getSubscribedGroupIds());
    setProfile(account);
  }

  @Override
  public void onError(Throwable e) {

  }

  @Override
  public void onEmpty() {

  }

  @NonNull
  @Override
  public ProfileEditPresenter createPresenter() {
    return new ProfileEditPresenter(UserRepositoryProvider.getRepository());
  }

  @OnTextChanged(R.id.et_profile_edit_realname)
  void listenRealNameChange(CharSequence text) {
    tilRealName.setError(TextUtils.isEmpty(text) ? realNameEmptyErrorString : null);
    realNameErrorSubject.onNext(!TextUtils.isEmpty(text));
  }

  @OnTextChanged(R.id.et_profile_edit_username)
  void listenUsernameChange(CharSequence text) {
    usernameSubject.onNext(text.toString());
    tilUsername.setError(TextUtils.isEmpty(text) ? usernameEmptyErrorString : null);
    usernameErrorSubject.onNext(!TextUtils.isEmpty(text));
  }

  @OnTextChanged(R.id.et_profile_edit_email)
  void listenEmailChange(CharSequence text) {
    tilEmail.setError(TextUtils.isEmpty(text) ? emailEmptyErrorString : null);
    tilEmail.setError(FormUtil.isEmailAddress(text) ? null : invalidEmailString);
    emailErrorSubject.onNext(!TextUtils.isEmpty(text) || !FormUtil.isEmailAddress(text));
  }

  @OnTextChanged(R.id.et_profile_edit_password)
  void listenPasswordChange(CharSequence text) {
    tilPassword.setError(FormUtil.isPasswordValid(text) ? null : invalidPasswordString);
    tilPassword.setError(TextUtils.isEmpty(text) ? null : passwordEmptyErrorString);
    passwordErrorSubject.onNext(!TextUtils.isEmpty(text) || !FormUtil.isPasswordValid(text));
  }

  @OnTextChanged(R.id.et_profile_edit_website)
  void listenWebsiteChange(CharSequence text) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    tilWebsite.setError(FormUtil.isWebUrl(text) ? null : invalidWebsiteString);
    websiteErrorSubject.onNext(!FormUtil.isWebUrl(text));
  }

  @OnClick(R.id.til_profile_edit_country)
  void changeCountry() {
    countryPicker = new CountryPickerDialog(getActivity(), (country, flagResId) -> {
      tilCountry.getEditText().setText(country.getCountryName(getActivity()));
      if (!draft.getCountry().getCode().equals(country.getIsoCode())) {
        draft.setCountry(new CountryRealm(country.getIsoCode()));
      }
    }, false, 0);
    countryPicker.show();
  }

  @OnClick({
      R.id.iv_profile_edit_avatar, R.id.tv_profile_edit_change_photo
  })
  void changeAvatar() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.label_editor_select_media_source_title)
        .setItems(R.array.action_editor_list_media_source, (dialog, which) -> {
          KeyboardUtil.hideKeyboard(getView());

          if (which == 0) {
            checkGalleryPermissions();
          } else if (which == 1) {
            checkCameraPermissions();
          }
        })
        .show();
  }

  @OnClick(R.id.iv_profile_edit_save)
  void saveProfile() {
    if (!isValid()) {
      return;
    }

    String realName = tilRealName.getEditText().getText().toString();
    if (!realName.equals(original.getRealname())) {
      draft.setRealname(realName);
    }

    String username = tilUsername.getEditText().getText().toString();
    if (!username.equals(original.getUsername())) {
      draft.setUsername(username);
    }

    String email = tilEmail.getEditText().getText().toString();
    if (!email.equals(original.getEmail())) {
      draft.setEmail(email);
    }

    if (tilPassword.isEnabled()) {
      String password = tilPassword.getEditText().getText().toString();
      // TODO: 19.03.2017 Implement password issue.
    }

    String gender = spinnerGender.getSelectedItem().toString();
    if (!gender.equalsIgnoreCase(original.getGender())) {
      draft.setGender(gender.toUpperCase());
    }

    String websiteUrl = tilWebsite.getEditText().getText().toString();
    if (!websiteUrl.equals(original.getWebsiteUrl())) {
      if (!websiteUrl.contains("http://") || websiteUrl.contains("https://")) {
        websiteUrl = "http://" + websiteUrl;
      }
      draft.setWebsiteUrl(websiteUrl);
    }

    String bio = tilBio.getEditText().getText().toString();
    if (!bio.equals(original.getBio())) {
      draft.setBio(bio);
    }

    getPresenter().updateMe(draft);

    Timber.d("Changes will be saved: %s", draft.toString());
  }

  private boolean isValid() {
    if (TextUtils.isEmpty(tilRealName.getEditText().getText())) {
      showMessage(realNameEmptyErrorString);
      return false;
    }

    if (TextUtils.isEmpty(tilUsername.getEditText().getText())) {
      showMessage(usernameEmptyErrorString);
      return false;
    }

    if (TextUtils.isEmpty(tilEmail.getEditText().getText())) {
      showMessage(emailEmptyErrorString);
      return false;
    }

    if (FormUtil.isEmailAddress(tilWebsite.getEditText().getText())) {
      showMessage(invalidWebsiteString);
      return false;
    }

    return true;
  }

  private void setProfile(AccountRealm account) {
    Glide
        .with(getActivity())
        .load(account.getAvatarUrl().replace("s96-c", "s80-c-rw"))
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(ivAvatar);

    tilRealName.getEditText().setText(account.getRealname());
    tilUsername.getEditText().setText(account.getUsername());
    tilEmail.getEditText().setText(account.getEmail());

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
      tilPassword.setEnabled(user.getProviderId().equals(AuthUI.EMAIL_PROVIDER));
      tilEmail.setEnabled(user.getProviderId().equals(AuthUI.EMAIL_PROVIDER));
    }

    tilCountry.getEditText().setText(account.getCountry().getName());

    ArrayAdapter adapter = (ArrayAdapter) spinnerGender.getAdapter();
    final String gender = account.getGender();
    // Convert gender string to spinner version
    // E.g. female -> Female
    final String spinnerGenderText = gender.substring(0, 1).toUpperCase() + gender.substring(1);
    final int spinnerPosition = adapter.getPosition(spinnerGenderText);
    spinnerGender.setSelection(spinnerPosition);

    tilWebsite.getEditText().setText(account.getWebsiteUrl());
    TextViewUtil.stripUnderlines(tilWebsite.getEditText().getText());
    tilBio.getEditText().setText(account.getBio());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setTitle(R.string.label_profile_edit_title);
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setHomeAsUpIndicator(
        AppCompatResources.getDrawable(getActivity(), R.drawable.ic_close_white_24dp));
  }

  private void checkCameraPermissions() {
    new Permissive.Request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO)
        .whenPermissionsGranted(permissions -> openCamera())
        .whenPermissionsRefused(permissions -> Snackbar
            .make(getView(), "Permission is denied!", Snackbar.LENGTH_SHORT)
            .show())
        .execute(getActivity());
  }

  private void checkGalleryPermissions() {
    new Permissive.Request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .whenPermissionsGranted(permissions -> openGallery())
        .whenPermissionsRefused(permissions -> Snackbar
            .make(getView(), "Permission is denied!", Snackbar.LENGTH_SHORT)
            .show())
        .execute(getActivity());
  }

  private void openGallery() {
    Intent intent =
        new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");

    startActivityForResult(
        Intent.createChooser(intent, getResources().getString(R.string.label_select_media)),
        REQUEST_SELECT_MEDIA);
  }

  private void openCamera() {
    final Activity activity = getActivity();

    if (CameraHelper.hasCamera(activity)) {
      Intent cameraIntent = new Intent(activity,
          CameraHelper.hasCamera2(activity) ? Camera2Activity.class : Camera1Activity.class);

      cameraIntent.putExtra(CameraConfiguration.Arguments.REQUEST_CODE, REQUEST_CAPTURE_MEDIA);
      cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_PICKER, false);
      cameraIntent.putExtra(CameraConfiguration.Arguments.MEDIA_ACTION,
          CameraConfiguration.MEDIA_ACTION_PHOTO);
      cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CROP, false);

      startActivityForResult(cameraIntent, REQUEST_CAPTURE_MEDIA);
    }
  }

  private void handleGalleryResult(Intent data) {
    final Uri uri = data.getData();
    if (uri != null) {
      startCropActivity(uri);
    }
  }

  private void handleCameraResult(Intent data) {
    final String path = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
    if (path != null) {
      MediaUtil.addToPhoneGallery(path, getActivity());

      startCropActivity(Uri.fromFile(new File(path)));
    }
  }

  private void startCropActivity(Uri uri) {
    final Uri destUri =
        Uri.fromFile(new File(YolooApp.getCacheDirectory(), MediaUtil.createImageName()));

    Intent intent = UCrop
        .of(uri, destUri)
        .withAspectRatio(1, 1)
        .withMaxResultSize(180, 180)
        .withOptions(createUCropOptions())
        .getIntent(getActivity());

    startActivityForResult(intent, UCrop.REQUEST_CROP);
  }

  private void handleCropResult(Intent data) {
    final Uri uri = UCrop.getOutput(data);
    if (uri != null) {
      draft.setAvatarUrl(uri.getPath());

      Glide
          .with(getActivity())
          .load(uri)
          .bitmapTransform(new CropCircleTransformation(getActivity()))
          .into(ivAvatar);
    } else {
      Toast.makeText(getActivity(), "Error occurred.", Toast.LENGTH_SHORT).show();
    }
  }

  private void handleCropError(Intent data) {
    Timber.e("Crop error: %s", UCrop.getError(data));
  }

  private UCrop.Options createUCropOptions() {
    final UCrop.Options options = new UCrop.Options();
    options.setCompressionFormat(Bitmap.CompressFormat.WEBP);
    options.setCompressionQuality(85);
    options.setToolbarColor(primaryColor);
    options.setStatusBarColor(primaryDarkColor);
    options.setToolbarTitle(getResources().getString(R.string.label_editor_crop_image_title));
    return options;
  }

  private void showMessage(String message) {
    Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
  }
}
