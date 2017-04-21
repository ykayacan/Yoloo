package com.yoloo.android.feature.editor.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.Controller;
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
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.MediaRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.data.repository.tag.datasource.TagDiskDataStore;
import com.yoloo.android.data.repository.tag.datasource.TagRemoteDataStore;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.editor.EditorType;
import com.yoloo.android.feature.editor.selectbounty.BountySelectController;
import com.yoloo.android.feature.editor.selectgroup.SelectGroupController;
import com.yoloo.android.feature.editor.tagselectdialog.TagSelectDialog;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.widget.EditorCoverView;
import com.yoloo.android.ui.widget.ThumbView;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.Connectivity;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MediaUtil;
import com.yoloo.android.util.TextViewUtil;
import java.io.File;
import java.util.Date;
import java.util.List;
import timber.log.Timber;

public class EditorController extends MvpController<EditorView, EditorPresenter>
    implements EditorView, TagSelectDialog.OnTagsSelectedListener {

  private static final String KEY_EDITOR_TYPE = "EDITOR_TYPE";

  private static final int REQUEST_SELECT_MEDIA = 1;
  private static final int REQUEST_CAPTURE_MEDIA = 2;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.et_compose_area) EditText etEditor;
  @BindView(R.id.tv_editor_post) TextView tvPost;
  @BindView(R.id.editor_tag_container) ViewGroup tagContainer;
  @BindView(R.id.tv_editor_select_group) TextView tvSelectGroup;

  @Nullable @BindView(R.id.view_cover_1) EditorCoverView coverView1;
  @Nullable @BindView(R.id.view_cover_2) EditorCoverView coverView2;
  @Nullable @BindView(R.id.view_cover_3) EditorCoverView coverView3;

  @Nullable @BindView(R.id.image_area_container) ViewGroup imageContainer;
  @Nullable @BindView(R.id.tv_ask_bounty) TextView tvAskBounty;
  @Nullable @BindView(R.id.iv_blogeditor_cover) ImageView ivBlogCover;
  @Nullable @BindView(R.id.et_blogeditor_title) EditText etBlogTitle;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;

  @BindDrawable(R.drawable.dialog_tag_bg) Drawable tagBgDrawable;

  @BindDimen(R.dimen.spacing_normal) int normalSpaceDimen;

  @BindString(R.string.label_compose_select_group) String selectGroupString;

  private PostRealm draft;

  private int editorType;

  public EditorController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setHasOptionsMenu(true);

    editorType = getArgs().getInt(KEY_EDITOR_TYPE);
  }

  public static EditorController create(@EditorType int editorType) {
    final Bundle bundle = new BundleBuilder().putInt(KEY_EDITOR_TYPE, editorType).build();

    return new EditorController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    final int layoutRes = editorType == EditorType.ASK_QUESTION
        ? R.layout.controller_editor_question
        : R.layout.controller_editor_blog;

    return inflater.inflate(layoutRes, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();

    tvPost.setEnabled(false);
    etEditor.requestFocus();

    if (coverView1 != null) {
      coverView1.setOnAddImageListener(this::showAddPhotoDialog);
    }

    if (coverView2 != null) {
      coverView2.setOnAddImageListener(this::showAddPhotoDialog);
    }

    if (coverView3 != null) {
      coverView3.setOnAddImageListener(this::showAddPhotoDialog);
    }

    ControllerUtil.preventDefaultBackPressAction(view, this::showDiscardDraftDialog);
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    if (changeType.equals(ControllerChangeType.POP_ENTER)) {
      //getPresenter().loadDraft();
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      KeyboardUtil.hideKeyboard(etEditor);
      showDiscardDraftDialog();
      /*setTempDraft();
      getPresenter().updateDraft(draft, EditorPresenter.NAV_BACK);*/
      return false;
    }

    return super.onOptionsItemSelected(item);
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
    return new EditorPresenter(
        TagRepository.getInstance(TagRemoteDataStore.getInstance(), TagDiskDataStore.getInstance()),
        PostRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  @Override
  public void onDraftCreated(PostRealm draft) {

  }

  @Override
  public void onDraftUpdated(int navigation) {
    if (navigation == EditorPresenter.NAV_SELECT_GROUP) {
      Controller controller = SelectGroupController.create();
      controller.setTargetController(this);

      RouterTransaction transaction = RouterTransaction
          .with(controller)
          .pushChangeHandler(new VerticalChangeHandler())
          .popChangeHandler(new VerticalChangeHandler());

      getRouter().pushController(transaction);
    } else if (navigation == EditorPresenter.NAV_BOUNTY) {
      getRouter().pushController(RouterTransaction
          .with(BountySelectController.create())
          .pushChangeHandler(new VerticalChangeHandler())
          .popChangeHandler(new VerticalChangeHandler()));
    } else if (navigation == EditorPresenter.NAV_POST) {
      showTagDialog();
    } else if (navigation == EditorPresenter.NAV_SEND) {
      /*Intent intent = new Intent(getActivity(), SendPostService.class);
      getActivity().startService(intent);*/

      getRouter().popToRoot();
    }
  }

  @Override
  public void onError(Throwable t) {
    Timber.e(t);
  }

  @Override
  public void onRecommendedTagsLoaded(List<TagRealm> tags) {

  }

  @Override
  public void onSearchTags(List<TagRealm> tags) {

  }

  @Optional
  @OnTextChanged({
      R.id.et_compose_area, R.id.et_blogeditor_title
  })
  void listenInputChanges(CharSequence content) {
    if (editorType == EditorType.ASK_QUESTION) {
      tvPost.setEnabled(!TextUtils.isEmpty(content.toString().trim()));
    } else if (editorType == EditorType.BLOG) {
      tvPost.setEnabled(!TextUtils.isEmpty(content.toString().trim()) && !TextUtils.isEmpty(
          etBlogTitle.getText()));
    }
  }

  @Optional
  @OnClick(R.id.tv_ask_bounty)
  void showBounties() {
    if (Connectivity.isConnected(getApplicationContext())) {
      KeyboardUtil.hideKeyboard(etEditor);
      setTempDraft();

      getPresenter().updateDraft(draft, EditorPresenter.NAV_BOUNTY);
    } else {
      Snackbar.make(getView(), R.string.error_bounty_network, Snackbar.LENGTH_LONG).show();
    }
  }

  private void showTagDialog() {
    TagSelectDialog dialog = new TagSelectDialog(getActivity());
    dialog.setOnTagsSelectedListener(this);
    dialog.setInitialTags(getUsedTags());
    dialog.show();
  }

  @Optional
  @OnClick(R.id.ib_add_photo)
  void showAddPhotoDialog() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.label_editor_select_media_source_title)
        .setItems(R.array.action_editor_list_media_source, (dialog, which) -> {
          KeyboardUtil.hideKeyboard(etEditor);

          if (which == 0) {
            checkGalleryPermissions();
          } else if (which == 1) {
            checkCameraPermissions();
          }
        })
        .show();
  }

  @OnClick(R.id.tv_editor_post)
  void createNewPost() {
    KeyboardUtil.hideKeyboard(etEditor);

    if (tvSelectGroup.getText().equals(selectGroupString)) {
      Snackbar.make(getView(), "Select a group!", Snackbar.LENGTH_SHORT).show();
    } else {
      setTempDraft();
      getPresenter().updateDraft(draft, EditorPresenter.NAV_POST);
    }
  }

  @Optional
  @OnClick(R.id.iv_blogeditor_cover)
  void removeCover() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.label_editor_delete_cover_image)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          ivBlogCover.setImageDrawable(null);
          ivBlogCover.setVisibility(View.GONE);
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  @Optional
  @OnClick(R.id.tv_editor_select_group)
  void openGroupSelectScreen() {
    getPresenter().updateDraft(draft, EditorPresenter.NAV_SELECT_GROUP);
  }

  @Override
  public void onTagsSelected(List<TagRealm> tags) {
    Stream.of(tags).forEach(tagRealm -> draft.addTag(tagRealm));

    getPresenter().updateDraft(draft, EditorPresenter.NAV_SEND);

    /*tagContainer.setVisibility(View.VISIBLE);
    tagContainer.removeAllViews();

    Stream.of(tagNames).forEach(tagName -> {
      final TextView tag = new TextView(getApplicationContext());
      tag.setText(getActivity().getString(R.string.label_tag, tagName));
      tag.setGravity(Gravity.CENTER);
      tag.setPadding(16, 10, 16, 10);
      tag.setBackground(tagBgDrawable);
      TextViewUtil.setTextAppearance(tag, getActivity(), R.style.TextAppearance_AppCompat);

      tagContainer.addView(tag);
    });*/
  }

  private void setTempDraft() {
    draft.setContent(etEditor.getText().toString());

    Stream.of(getUsedTags()).map(s -> new TagRealm().setName(s)).forEach(tag -> draft.addTag(tag));

    draft.setCreated(new Date());

    if (etBlogTitle != null) {
      draft.setTitle(etBlogTitle.getText().toString());
    }

    if (editorType == EditorType.ASK_QUESTION) {
      draft.setPostType(draft.getMedias().isEmpty() ? PostRealm.TYPE_TEXT : PostRealm.TYPE_RICH);
    } else if (editorType == EditorType.BLOG) {
      draft.setPostType(PostRealm.TYPE_BLOG);
    }
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayShowTitleEnabled(false);
    ab.setDisplayHomeAsUpEnabled(true);
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
        .withMaxResultSize(800, 800)
        .withOptions(createUCropOptions())
        .getIntent(getActivity());

    startActivityForResult(intent, UCrop.REQUEST_CROP);
  }

  private void handleCropResult(Intent data) {
    final Uri uri = UCrop.getOutput(data);
    if (uri == null) {
      Toast.makeText(getActivity(), "Error occurred.", Toast.LENGTH_SHORT).show();
    } else {
      if (editorType == EditorType.ASK_QUESTION) {
        addThumbView(uri);
      } else if (editorType == EditorType.BLOG) {
        addCoverView(uri);
      }
    }
  }

  private void handleCropError(Intent data) {
    Timber.e("Crop error: %s", UCrop.getError(data));
  }

  private void addThumbView(Uri uri) {
    imageContainer.setVisibility(View.VISIBLE);

    final ThumbView thumbView = new ThumbView(getApplicationContext());

    Glide.with(getActivity()).load(uri).override(90, 90).into(new SimpleTarget<GlideDrawable>() {
      @Override
      public void onResourceReady(GlideDrawable resource,
          GlideAnimation<? super GlideDrawable> glideAnimation) {
        thumbView.setImageDrawable(resource);
      }
    });

    thumbView.setListener(view -> {
      imageContainer.removeView(view);

      if (imageContainer.getChildCount() == 0) {
        imageContainer.setVisibility(View.GONE);
      }
    });

    // Clear container before adding an image.
    imageContainer.removeAllViews();
    imageContainer.addView(thumbView);

    MediaRealm media = new MediaRealm();
    media.setTempPath(uri.getPath());

    draft.addMedia(media);
  }

  private void addCoverView(Uri uri) {
    ivBlogCover.setVisibility(View.VISIBLE);
    ivBlogCover.setImageURI(uri);

    MediaRealm media = new MediaRealm();
    media.setTempPath(uri.getPath());

    draft.addMedia(media);
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

  private void setEditorContentFromDraft(PostRealm draft) {
    if (editorType == EditorType.ASK_QUESTION) {
      tvAskBounty.setText(draft.getBounty() == 0
          ? getResources().getString(R.string.action_editor_add_bounty)
          : String.valueOf(draft.getBounty()));

      if (!draft.getMedias().isEmpty()) {
        addThumbView(Uri.fromFile(new File(draft.getMedias().get(0).getTempPath())));
      }
    } else if (editorType == EditorType.BLOG) {
      if (!TextUtils.isEmpty(draft.getTitle())) {
        etBlogTitle.setText(draft.getTitle());
        etBlogTitle.setSelection(draft.getTitle().length());
      }

      if (!draft.getMedias().isEmpty()) {
        addCoverView(Uri.fromFile(new File(draft.getMedias().get(0).getTempPath())));
      }
    }

    if (!TextUtils.isEmpty(draft.getContent())) {
      etEditor.setText(draft.getContent());
      etEditor.setSelection(draft.getContent().length());
    }

    if (!draft.getTags().isEmpty()) {
      tagContainer.setVisibility(View.VISIBLE);
      tagContainer.removeAllViews();

      Stream.of(draft.getTags()).map(TagRealm::getName).forEach(tagName -> {
        final TextView tag = new TextView(getApplicationContext());
        tag.setText(getActivity().getString(R.string.label_tag, tagName));
        tag.setGravity(Gravity.CENTER);
        tag.setPadding(16, 10, 16, 10);
        tag.setBackground(tagBgDrawable);
        TextViewUtil.setTextAppearance(tag, getActivity(), R.style.TextAppearance_AppCompat);

        tagContainer.addView(tag);
      });
    }
  }

  private List<String> getUsedTags() {
    return Stream
        .rangeClosed(0, tagContainer.getChildCount())
        .map(index -> tagContainer.getChildAt(index))
        .select(TextView.class)
        .map(textView -> textView.getText().toString().replace("#", ""))
        .toList();
  }

  private void showDiscardDraftDialog() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.action_catalog_discard_draft)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          getPresenter().deleteDraft();
          getRouter().popToRoot();
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  public void onGroupSelected(GroupRealm group) {
    draft.setGroupId(group.getId());

    tvSelectGroup.setText(group.getName());
  }
}
