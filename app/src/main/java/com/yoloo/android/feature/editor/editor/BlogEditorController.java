package com.yoloo.android.feature.editor.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.github.jksiezni.permissive.Permissive;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;
import com.yalantis.ucrop.UCrop;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.db.GroupRealm;
import com.yoloo.android.data.db.MediaRealm;
import com.yoloo.android.data.db.PostRealm;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.tag.TagRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.editor.selectgroup.SelectGroupController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.ui.widget.AutoCompleteTagAdapter;
import com.yoloo.android.ui.widget.ChipAdapter;
import com.yoloo.android.ui.widget.SliderView;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.DrawableHelper;
import com.yoloo.android.util.HtmlUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MediaUtil;
import com.yoloo.android.util.WeakHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class BlogEditorController extends MvpController<EditorView, EditorPresenter>
    implements EditorView, ChipAdapter.OnItemSelectListener<TagRealm>,
    SelectGroupController.Groupable {

  private static final int REQUEST_SELECT_MEDIA = 1;
  private static final int REQUEST_CAPTURE_MEDIA = 2;

  private final WeakHandler handler = new WeakHandler();

  @BindView(R.id.root_view) ViewGroup rootView;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.slider) SliderView sliderView;
  @BindView(R.id.et_blog_content) EditText etEditorContent;
  @BindView(R.id.et_blog_title) EditText etTitle;
  @BindView(R.id.tv_blog_user_info) TextView tvUserInfo;
  @BindView(R.id.tv_editor_post_select_group) TextView tvEditorSelectGroup;
  @BindView(R.id.et_editor_post_tags) NachoTextView etEditorTags;
  @BindView(R.id.recycler_view) RecyclerView rvEditorTrendingTags;
  @BindView(R.id.tv_editor_post_add_bounty) TextView tvEditorAddBounty;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;
  @BindColor(android.R.color.secondary_text_light) int secondaryTextColor;

  @BindDimen(R.dimen.spacing_micro) int microSpaceDimen;

  @BindString(R.string.label_editor_select_group) String selectGroupString;

  private List<Uri> selectedImageUris = new ArrayList<>(5);
  private List<TagRealm> selectedTags = new ArrayList<>(7);

  private ChipAdapter<TagRealm> tagAdapter;
  private AutoCompleteTagAdapter tagAutoCompleteAdapter;

  private PostRealm draft;

  public static BlogEditorController create() {
    return new BlogEditorController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_editor_blog, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setHasOptionsMenu(true);
    setupToolbar();
    setupRecyclerView();
    setupChipTextView();
    tintIcons();

    tvEditorSelectGroup.setText(
        HtmlUtil.fromHtml(getActivity(), R.string.label_editor_select_group));

    ControllerUtil.preventDefaultBackPressAction(view, this::showDiscardDraftDialog);
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);

    tagAutoCompleteAdapter
        .getQuery()
        .filter(s -> !s.isEmpty())
        .filter(s -> s.length() > 2)
        .debounce(400, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> getPresenter().searchTag(s));
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_editor, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();

    KeyboardUtil.hideKeyboard(getView());

    if (itemId == android.R.id.home) {
      showDiscardDraftDialog();
    } else if (itemId == R.id.action_share && isValidToSend()) {
      setTempDraft();
      getPresenter().updateDraft(draft);
    }

    return false;
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
    return new EditorPresenter(TagRepositoryProvider.getRepository(),
        PostRepositoryProvider.getRepository(), UserRepositoryProvider.getRepository());
  }

  @Override
  public void onMeLoaded(AccountRealm me) {
    tvUserInfo.setText(getResources().getString(R.string.label_blog_user_info, me.getUsername(),
        me.getLevelTitle()));
  }

  @Override
  public void onDraftCreated(PostRealm draft) {
    this.draft = draft;
  }

  @Override
  public void onDraftUpdated() {
    getPresenter().sendPost();

    getRouter().handleBack();
  }

  @Override
  public void onError(Throwable t) {
    Timber.e(t);
  }

  @Override
  public void onTrendingTagsLoaded(List<TagRealm> tags) {
    tagAdapter.addChipItems(tags);
  }

  @Override
  public void onSearchTags(List<TagRealm> tags) {
    tagAutoCompleteAdapter.replaceItems(tags);
    handler.post(etEditorTags::showDropDown);
  }

  @OnClick(R.id.iv_add_image)
  void openAddMediaDialog() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.label_editor_select_media_source_title)
        .setItems(R.array.action_editor_list_media_source, (dialog, which) -> {
          KeyboardUtil.hideKeyboard(etEditorContent);

          if (which == 0) {
            checkGalleryPermissions();
          } else if (which == 1) {
            checkCameraPermissions();
          }
        })
        .show();
  }

  @OnClick(R.id.tv_editor_post_select_group)
  void openSelectGroupScreen() {
    Controller controller = SelectGroupController.create();
    controller.setTargetController(this);

    getRouter().pushController(RouterTransaction
        .with(controller)
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  @OnClick(R.id.tv_editor_post_add_bounty)
  void openAddBountyScreen() {
    Snackbar
        .make(getView(), "Adding bounty feature will be available soon", Snackbar.LENGTH_SHORT)
        .show();
  }

  private void setTempDraft() {
    draft.setTitle(etTitle.getText().toString());

    // Set current content.
    draft.setContent(etEditorContent.getText().toString());

    // Set all tags.
    Stream
        .of(etEditorTags.getAllChips())
        .map(Chip::getText)
        .map(CharSequence::toString)
        .distinct()
        .map(String::toLowerCase)
        .map(tagName -> new TagRealm().setId(tagName).setName(tagName))
        .forEach(tag -> draft.addTag(tag));

    // Set image Uris
    Stream
        .of(selectedImageUris)
        .map(uri -> new MediaRealm().setTempPath(uri.getPath()))
        .forEach(media -> draft.addMedia(media));

    // Set post type.
    draft.setPostType(PostRealm.TYPE_BLOG);
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

  private void handleGalleryResult(Intent data) {
    final Uri uri = data.getData();
    if (uri != null) {
      startCropActivity(uri);
    }
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
      addThumbView(uri);
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

  private void addThumbView(Uri uri) {
    selectedImageUris.add(uri);

    sliderView.addImageUrl(uri.getPath());
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

  @Override
  public void onItemSelect(View v, EpoxyModel<?> model, TagRealm item, boolean selected) {
    if (selectedTags.size() == 7) {
      Toast.makeText(getActivity(), "You have selected 7 tags", Toast.LENGTH_SHORT).show();
      tagAdapter.toggleSelection(model);
      return;
    }

    if (selected) {
      if (etEditorTags.isAlreadyAdded(item.getName())) {
        tagAdapter.toggleSelection(model);
      } else {
        etEditorTags.insertChip(item.getName());
        selectedTags.add(item);
      }
    } else {
      etEditorTags.removeChip(item.getName());
      selectedTags.remove(item);
    }
  }

  private void setupRecyclerView() {
    tagAdapter = new ChipAdapter<>(this);
    tagAdapter.setBackgroundDrawable(R.drawable.chip_tag_bg);

    rvEditorTrendingTags.setLayoutManager(
        new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
    rvEditorTrendingTags.setItemAnimator(new DefaultItemAnimator());
    rvEditorTrendingTags.addItemDecoration(
        new SpaceItemDecoration(microSpaceDimen, SpaceItemDecoration.HORIZONTAL));
    rvEditorTrendingTags.setHasFixedSize(true);
    rvEditorTrendingTags.setAdapter(tagAdapter);
  }

  private void setupChipTextView() {
    tagAutoCompleteAdapter = new AutoCompleteTagAdapter(getActivity(), etEditorTags);

    etEditorTags.setAdapter(tagAutoCompleteAdapter);
    etEditorTags.setIllegalCharacters('\"', '.', '~');
    etEditorTags.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
    etEditorTags.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
    etEditorTags.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
    etEditorTags.setNachoValidator(new ChipifyingNachoValidator());
    etEditorTags.enableEditChipOnTouch(true, true);
    etEditorTags.setOnChipClickListener((chip, motionEvent) -> {

    });
  }

  public void onGroupSelected(GroupRealm group) {
    draft.setGroupId(group.getId());
    tvEditorSelectGroup.setText(group.getName());
  }

  private void showMessage(String message) {
    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
  }

  private boolean isValidToSend() {
    if (selectedImageUris.isEmpty()) {
      showMessage("You must add at least one cover image to blog!");
      return false;
    }

    if (TextUtils.isEmpty(etTitle.getText().toString())) {
      showMessage("You must write a blog title!");
      return false;
    }

    if (etEditorContent.getText().toString().isEmpty()) {
      showMessage("You must add content to blog!");
      return false;
    }

    if (tvEditorSelectGroup.getText().equals(selectGroupString)) {
      showMessage("Select a group!");
      return false;
    }

    if (etEditorTags.getChipValues().isEmpty()) {
      showMessage("Tags are empty!");
      return false;
    }

    return true;
  }

  private void tintIcons() {
    DrawableHelper
        .create()
        .withDrawable(tvEditorSelectGroup.getCompoundDrawables()[0])
        .withColor(secondaryTextColor)
        .tint();

    DrawableHelper
        .create()
        .withDrawable(etEditorTags.getCompoundDrawables()[0])
        .withColor(secondaryTextColor)
        .tint();
  }
}
