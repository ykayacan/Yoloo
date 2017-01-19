package com.yoloo.android.feature.write.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import com.github.jksiezni.permissive.Permissive;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;
import com.yalantis.ucrop.UCrop;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.feature.base.framework.MvpController;
import com.yoloo.android.feature.ui.AutoCompleteTagAdapter;
import com.yoloo.android.feature.ui.SpaceTokenizer;
import com.yoloo.android.feature.ui.widget.TagAutoCompleteTextView;
import com.yoloo.android.feature.ui.widget.ThumbView;
import com.yoloo.android.feature.ui.widget.tagview.TagView;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.WeakHandler;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import timber.log.Timber;

public class EditorController extends MvpController<EditorView, EditorPresenter>
    implements EditorView, AutoCompleteTagAdapter.OnAutoCompleteListener {

  private static final TagView.DataTransform<TagRealm> TRANSFORMER = TagRealm::getName;

  private static final int REQUEST_SELECT_MEDIA = 1;
  private static final int REQUEST_CAPTURE_MEDIA = 2;

  @BindView(R.id.toolbar_editor)
  Toolbar toolbar;

  @BindView(R.id.et_editor)
  EditText etEditor;

  @BindView(R.id.image_area_layout)
  ViewGroup imageArea;

  @BindColor(R.color.primary)
  int primaryColor;

  @BindColor(R.color.primary_dark)
  int primaryDarkColor;

  private View tagDialogView;

  private AlertDialog tagDialog;

  private TagAutoCompleteTextView tvTagAutoComplete;

  private AutoCompleteTagAdapter tagAdapter;
  private WeakHandler handler = new WeakHandler();
  private Runnable tagDropdownRunnable;

  // Enable or disable post action according to content changes.
  private boolean hasContent;

  public EditorController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_editor, container, false);
  }

  @Override
  protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupToolbar();
    setHasOptionsMenu(true);
    setupTagDialog();
    setupTagAutoCompleteAdapter();
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    menu.getItem(0).setEnabled(hasContent);
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_editor, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        processBackButton();
        return true;
      case R.id.action_post:
        Snackbar.make(getView(), "Sending...", Snackbar.LENGTH_SHORT).show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
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

  @NonNull
  @Override
  public EditorPresenter createPresenter() {
    return new EditorPresenter(TagRepository.getInstance(TagRemoteDataStore.getInstance(),
        TagDiskDataStore.getInstance()));
  }

  @Override
  public void onRecommendedTagsLoaded(List<TagRealm> tags) {
    final TagView tagView =
        ButterKnife.findById(tagDialogView, R.id.tagview_overlay_recommended_tags);
    tagView.setData(tags, TRANSFORMER);
  }

  @Override
  public void onSuggestedTagsLoaded(List<TagRealm> tags) {
    tagAdapter.setItems(tags);
    handler.post(tagDropdownRunnable);
  }

  @Override
  public void onError(Throwable t) {

  }

  @Override
  public void onAutoCompleteFilter(String filtered) {
    getPresenter().loadSuggestedTags(filtered);
  }

  @OnTextChanged(R.id.et_editor)
  void listenInputChanges(CharSequence text) {
    hasContent = !TextUtils.isEmpty(text.toString().trim());
    getActivity().invalidateOptionsMenu();
  }

  @OnClick(R.id.tv_ask_bounty)
  void showBounties() {

  }

  @OnClick(R.id.ib_add_tag)
  void showTagDialog() {
    tagDialog.show();

    getPresenter().loadRecommendedTags();
  }

  @OnClick(R.id.ib_add_photo)
  void showAddPhotoDialog() {
    tagDialog =
        new AlertDialog.Builder(getActivity()).setTitle(R.string.label_select_media_source_title)
            .setItems(R.array.action_list_add_media, (dialog, which) -> {
              KeyboardUtil.hideKeyboard(getApplicationContext(), etEditor);

              switch (which) {
                case 0:
                  checkGalleryPermissions();
                  break;
                case 1:
                  checkCameraPermissions();
                  break;
              }
            })
            .show();
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // add back arrow to toolbar
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
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
      startCropActivity(Uri.fromFile(new File(path)));
    }
  }

  private void startCropActivity(Uri uri) {
    final Uri destUri = Uri.fromFile(new File(YolooApp.getCacheDirectory(), createImageName()));

    Intent intent = UCrop.of(uri, destUri)
        .withAspectRatio(1, 1)
        .withMaxResultSize(800, 800)
        .withOptions(createUCropOptions())
        .getIntent(getActivity());

    startActivityForResult(intent, UCrop.REQUEST_CROP);
  }

  private void handleCropResult(Intent data) {
    final Uri uri = UCrop.getOutput(data);
    if (uri != null) {
      addThumbView(uri);
    } else {
      Toast.makeText(getActivity(), "Error occurred.", Toast.LENGTH_SHORT).show();
    }
  }

  private void handleCropError(Intent data) {
    Timber.e("Crop error: %s", UCrop.getError(data));
  }

  private void addThumbView(Uri uri) {
    imageArea.setVisibility(View.VISIBLE);

    final ThumbView thumbView = new ThumbView(getApplicationContext());
    thumbView.setThumbPreview(uri);
    thumbView.setListener(view -> {
      imageArea.removeView(view);

      if (imageArea.getChildCount() == 0) {
        imageArea.setVisibility(View.GONE);
      }
    });

    // Clear container before adding an image.
    imageArea.removeAllViews();
    imageArea.addView(thumbView);
  }

  @NonNull
  private UCrop.Options createUCropOptions() {
    final UCrop.Options options = new UCrop.Options();
    options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
    options.setCompressionQuality(85);
    options.setToolbarColor(primaryColor);
    options.setStatusBarColor(primaryDarkColor);
    options.setToolbarTitle(getResources().getString(R.string.label_crop_image_title));
    return options;
  }

  @NonNull
  private String createImageName() {
    return "IMG_" + UUID.randomUUID().toString() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
        Locale.US).format(new Date(System.currentTimeMillis())) + ".jpg";
  }

  private void processBackButton() {
    if (tagDialog != null && tagDialog.isShowing()) {
      tagDialog.dismiss();
    }

    KeyboardUtil.hideKeyboard(getActivity(), etEditor);
    getRouter().popCurrentController();
  }

  private void checkCameraPermissions() {
    new Permissive.Request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO).whenPermissionsGranted(permissions -> openCamera())
        .whenPermissionsRefused(
            permissions -> Snackbar.make(getView(), "Permission is denied!", Snackbar.LENGTH_SHORT)
                .show())
        .execute(getActivity());
  }

  private void checkGalleryPermissions() {
    new Permissive.Request(Manifest.permission.WRITE_EXTERNAL_STORAGE).whenPermissionsGranted(
        permissions -> openGallery())
        .whenPermissionsRefused(
            permissions -> Snackbar.make(getView(), "Permission is denied!", Snackbar.LENGTH_SHORT)
                .show())
        .execute(getActivity());
  }

  private void setupTagAutoCompleteAdapter() {
    tvTagAutoComplete = ButterKnife.findById(tagDialogView, R.id.tv_tag_autocomplete);
    tagDropdownRunnable = tvTagAutoComplete::showDropDown;

    tagAdapter = new AutoCompleteTagAdapter(getActivity(), this);
    tvTagAutoComplete.setAdapter(tagAdapter);
    tvTagAutoComplete.setTokenizer(new SpaceTokenizer());
  }

  private void setupTagDialog() {
    tagDialogView = View.inflate(getActivity(), R.layout.dialog_editor_tag, null);

    tagDialog = new AlertDialog.Builder(getActivity()).setView(tagDialogView)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {

        })
        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        })
        .create();
  }
}