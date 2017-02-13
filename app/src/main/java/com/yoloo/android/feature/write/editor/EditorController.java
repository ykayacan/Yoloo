package com.yoloo.android.feature.write.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.jksiezni.permissive.Permissive;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;
import com.yalantis.ucrop.UCrop;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.post.datasource.PostDiskDataStore;
import com.yoloo.android.data.repository.post.datasource.PostRemoteDataStore;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.ui.widget.PhotoThumbnail;
import com.yoloo.android.ui.widget.ThumbView;
import com.yoloo.android.feature.write.CreatePostService;
import com.yoloo.android.feature.write.EditorType;
import com.yoloo.android.feature.write.bountyoverview.BountyController;
import com.yoloo.android.feature.write.tagoverview.TagOverviewDialog;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.KeyboardUtil;
import io.reactivex.annotations.Beta;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import timber.log.Timber;

public class EditorController extends MvpController<EditorView, EditorPresenter>
    implements EditorView {

  private static final String KEY_EDITOR_TYPE = "EDITOR_TYPE";

  private static final int REQUEST_SELECT_MEDIA = 1;
  private static final int REQUEST_CAPTURE_MEDIA = 2;

  @BindView(R.id.toolbar_editor) Toolbar toolbar;
  @BindView(R.id.et_editor) EditText etEditor;
  @BindView(R.id.tv_editor_post) TextView tvPost;

  @Nullable @BindView(R.id.image_area_layout) ViewGroup imageArea;
  @Nullable @BindView(R.id.tv_ask_bounty) TextView tvAskBounty;
  @Nullable @BindView(R.id.iv_blogeditor_cover) ImageView ivBlogCover;
  @Nullable @BindView(R.id.et_blogeditor_title) EditText etBlogTitle;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private PostRealm draft;

  private int editorType;

  public EditorController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
  }

  public static EditorController create(@EditorType int editorType) {
    final Bundle bundle = new BundleBuilder()
        .putInt(KEY_EDITOR_TYPE, editorType)
        .build();

    return new EditorController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    editorType = getArgs().getInt(KEY_EDITOR_TYPE);

    final int layoutRes = editorType == EditorType.ASK_QUESTION
            ? R.layout.controller_questioneditor
            : R.layout.controller_blogeditor;

    return inflater.inflate(layoutRes, container, false);
  }

  @Override protected void onViewCreated(@NonNull View view) {
    super.onViewCreated(view);
    setupToolbar();
    setHasOptionsMenu(true);
    setupTagDialog();

    tvPost.setEnabled(false);
    KeyboardUtil.showDelayedKeyboard(etEditor);
  }

  @Override protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    if (changeType.equals(ControllerChangeType.POP_ENTER)) {
      getPresenter().loadDraft();
    }
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    // handle arrow click here
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        KeyboardUtil.hideKeyboard(etEditor);
        setTempDraft();
        getPresenter().updateDraft(draft, EditorPresenter.NAV_BACK);
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

  @NonNull @Override public EditorPresenter createPresenter() {
    return new EditorPresenter(
        TagRepository.getInstance(
            TagRemoteDataStore.getInstance(),
            TagDiskDataStore.getInstance()),
        PostRepository.getInstance(
            PostRemoteDataStore.getInstance(),
            PostDiskDataStore.getInstance()));
  }

  @Override public void onDraftLoaded(PostRealm draft) {
    this.draft = draft;

    setEditorContentFromDraft(draft);
  }

  @Override public void onDraftSaved(int navigation) {
    if (navigation == EditorPresenter.NAV_BACK) {
      getRouter().handleBack();
    } else if (navigation == EditorPresenter.NAV_BOUNTY) {
      getRouter().pushController(RouterTransaction.with(new BountyController())
          .pushChangeHandler(new VerticalChangeHandler())
          .popChangeHandler(new VerticalChangeHandler()));
    } else if (navigation == EditorPresenter.NAV_POST) {
      Intent intent = new Intent(getActivity(), CreatePostService.class);
      getActivity().startService(intent);
      getRouter().popToRoot();
    }
  }

  @Override public void onError(Throwable t) {

  }

  @OnTextChanged(R.id.et_editor) void listenInputChanges(CharSequence text) {
    tvPost.setEnabled(!TextUtils.isEmpty(text.toString().trim()));
  }

  @Optional
  @OnClick(R.id.tv_ask_bounty) void showBounties() {
    KeyboardUtil.hideKeyboard(etEditor);
    setTempDraft();

    getPresenter().updateDraft(draft, EditorPresenter.NAV_BOUNTY);
  }

  @OnClick(R.id.ib_add_tag) void showTagDialog() {
    TagOverviewDialog dialog = new TagOverviewDialog(getActivity());
    dialog.show();
  }

  @OnClick(R.id.ib_add_photo) void showAddPhotoDialog() {
    new AlertDialog.Builder(getActivity()).setTitle(R.string.label_editor_select_media_source_title)
        .setItems(R.array.action_editor_list_media_source, (dialog, which) -> {
          KeyboardUtil.hideKeyboard(etEditor);

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

  @OnClick(R.id.tv_editor_post) void createNewPost() {
    setTempDraft();

    getPresenter().updateDraft(draft, EditorPresenter.NAV_POST);
    KeyboardUtil.hideKeyboard(etEditor);
  }

  @Optional @OnClick(R.id.iv_blogeditor_cover) void removeCover() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.label_editor_delete_cover_image)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          ivBlogCover.setImageDrawable(null);
          ivBlogCover.setVisibility(View.GONE);
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void setTempDraft() {
    draft.setContent(etEditor.getText().toString())
        .setCreated(new Date());

    if (etBlogTitle != null) {
      draft.setTitle(etBlogTitle.getText().toString());
    }

    if (editorType == EditorType.ASK_QUESTION) {
      draft.setType(draft.getMediaUrl() == null ? 0 : 1);
    } else if (editorType == EditorType.SHARE_TRIP) {
      draft.setType(2);
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPost back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
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
      if (editorType == EditorType.ASK_QUESTION) {
        addThumbView(uri);
      } else if (editorType == EditorType.SHARE_TRIP) {
        addCoverView(uri);
      }
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

    Glide.with(getActivity())
        .load(uri)
        .override(90, 90)
        .into(new SimpleTarget<GlideDrawable>() {
          @Override public void onResourceReady(GlideDrawable resource,
              GlideAnimation<? super GlideDrawable> glideAnimation) {
            thumbView.setImageDrawable(resource);
          }
        });

    thumbView.setListener(view -> {
      imageArea.removeView(view);

      if (imageArea.getChildCount() == 0) {
        imageArea.setVisibility(View.GONE);
      }
    });

    // Clear container before adding an image.
    imageArea.removeAllViews();
    imageArea.addView(thumbView);

    draft.setMediaUrl(uri.getPath());
  }

  @Beta
  private void addThumbView2(Uri uri) {
    imageArea.setVisibility(View.VISIBLE);

    final PhotoThumbnail thumbnail = new PhotoThumbnail(getApplicationContext());
    Glide.with(getActivity())
        .load(uri)
        .override(90, 90)
        .into(thumbnail);

    thumbnail.setCloseIcon(R.drawable.layer_remove);

    // Clear container before adding an image.
    imageArea.removeAllViews();
    imageArea.addView(thumbnail);

    //draft.setMediaUrl(uri.getPath());
  }

  private void addCoverView(Uri uri) {
    ivBlogCover.setVisibility(View.VISIBLE);
    ivBlogCover.setImageURI(uri);

    draft.setMediaUrl(uri.getPath());
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

  private String createImageName() {
    return "IMG_" + UUID.randomUUID().toString() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
        Locale.US).format(new Date(System.currentTimeMillis())) + ".webp";
  }

  private void checkCameraPermissions() {
    new Permissive.Request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO)
        .whenPermissionsGranted(permissions -> openCamera())
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

  private void setupTagDialog() {
    /*tagView.getSelectedItemsObservable()
        .flatMap(Observable::fromIterable)
        .cast(TagRealm.class)
        .map(TagRealm::getName)
        .toList()
        .doOnSuccess(tvTagAutoComplete::setTags)
        .subscribe();*/
  }

  private void setEditorContentFromDraft(PostRealm draft) {
    if (editorType == EditorType.ASK_QUESTION) {
      tvAskBounty.setText(draft.getBounty() == 0
          ? getResources().getString(R.string.action_editor_ask_bounty)
          : String.valueOf(draft.getBounty()));

      if (!TextUtils.isEmpty(draft.getMediaUrl())) {
        addThumbView(Uri.fromFile(new File(draft.getMediaUrl())));
      }
    } else if (editorType == EditorType.SHARE_TRIP) {
      if (!TextUtils.isEmpty(draft.getTitle())) {
        etBlogTitle.setText(draft.getTitle());
        etBlogTitle.setSelection(draft.getTitle().length());
      }

      if (!TextUtils.isEmpty(draft.getMediaUrl())) {
        addCoverView(Uri.fromFile(new File(draft.getMediaUrl())));
      }
    }

    if (!TextUtils.isEmpty(draft.getContent())) {
      etEditor.setText(draft.getContent());
      etEditor.setSelection(draft.getContent().length());
    }
  }
}