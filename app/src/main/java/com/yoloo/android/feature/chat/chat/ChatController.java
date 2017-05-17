package com.yoloo.android.feature.chat.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindView;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.jksiezni.permissive.Permissive;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;
import com.squareup.moshi.Moshi;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.yalantis.ucrop.UCrop;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.data.UploadManager;
import com.yoloo.android.data.UploadResponse;
import com.yoloo.android.data.chat.DefaultMessage;
import com.yoloo.android.data.chat.firebase.Chat;
import com.yoloo.android.data.chat.firebase.ChatMessage;
import com.yoloo.android.data.chat.firebase.ChatMessageBuilder;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.chat.chatlist.ChatListController;
import com.yoloo.android.feature.fullscreenphoto.FullscreenPhotoController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.MediaUtil;
import com.yoloo.android.util.ViewUtils;
import com.yoloo.android.util.glide.transfromation.CropCircleTransformation;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.Collections;
import java.util.List;
import timber.log.Timber;

public class ChatController extends BaseController
    implements MessageInput.InputListener, MessageInput.AttachmentsListener,
    MessagesListAdapter.OnMessageClickListener<DefaultMessage>,
    MessagesListAdapter.SelectionListener, MessagesListAdapter.OnLoadMoreListener {

  private static final int REQUEST_SELECT_MEDIA = 1;
  private static final int REQUEST_CAPTURE_MEDIA = 2;

  private static final String KEY_CHAT_ID = "CHAT_ID";

  private static final String KEY_ONE_ID = "ONE_ID";
  private static final String KEY_ONE_USERNAME = "ONE_USERNAME";
  private static final String KEY_ONE_AVATAR = "ONE_AVATAR";

  private static final String KEY_OTHER_ID = "OTHER_ID";
  private static final String KEY_OTHER_USERNAME = "OTHER_USERNAME";
  private static final String KEY_OTHER_AVATAR = "OTHER_AVATAR";

  @BindView(R.id.messagesList) MessagesList messagesList;
  @BindView(R.id.input) MessageInput messageInput;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindColor(R.color.primary) int primaryColor;
  @BindColor(R.color.primary_dark) int primaryDarkColor;

  private CompositeDisposable disposable;

  private MessagesListAdapter<DefaultMessage> adapter;

  private ChatRepository chatRepository;

  private String chatId;

  private String oneId;
  private String oneUsername;
  private String oneAvatar;

  private String otherId;
  private String otherUsername;
  private String otherAvatar;

  public ChatController(Bundle args) {
    super(args);
  }

  public static ChatController create(@NonNull String chatId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_CHAT_ID, chatId).build();
    return new ChatController(bundle);
  }

  public static ChatController create(@NonNull AccountRealm one, @NonNull AccountRealm other) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_ONE_ID, one.getId())
        .putString(KEY_ONE_USERNAME, one.getUsername())
        .putString(KEY_ONE_AVATAR, one.getAvatarUrl())
        .putString(KEY_OTHER_ID, other.getId())
        .putString(KEY_OTHER_USERNAME, other.getUsername())
        .putString(KEY_OTHER_AVATAR, other.getAvatarUrl())
        .build();

    return new ChatController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_chat, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    disposable = new CompositeDisposable();

    final Bundle args = getArgs();

    chatId = args.getString(KEY_CHAT_ID);

    oneId = args.getString(KEY_ONE_ID);
    oneUsername = args.getString(KEY_ONE_USERNAME);
    oneAvatar = args.getString(KEY_ONE_AVATAR);

    otherId = args.getString(KEY_OTHER_ID);
    otherUsername = args.getString(KEY_OTHER_USERNAME);
    otherAvatar = args.getString(KEY_OTHER_AVATAR);

    if (TextUtils.isEmpty(chatId)) {
      chatId = ChatRepository.createOneToOneChatId(oneId, otherId);
    }

    setupToolbar();
    setupRecyclerView();

    chatRepository = ChatRepository.getInstance();

    Disposable d = chatRepository.getChat(oneId, otherId)
        .subscribe(chat -> {
          if (!TextUtils.isEmpty(chat.getChatId())) {
            setChatToolbarInfo(chat);
          }
        }, Timber::e);
    disposable.add(d);

    Disposable d2 = getMessagesObservable(chatId)
        .subscribe(message -> adapter.addToStart(message, true), Timber::e);
    disposable.add(d2);

    ControllerUtil.preventDefaultBackPressAction(view, () -> {
      chatRepository.setLastSeen(oneId);
      KeyboardUtil.hideKeyboard(view);
      getRouter().popToTag(ChatListController.class.getName());
    });

    messageInput.setInputListener(this);
    messageInput.setAttachmentsListener(this);
  }

  @Override protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    ViewUtils.setStatusBarColor(getActivity(), Color.TRANSPARENT);
  }

  private void setChatToolbarInfo(Chat chat) {
    Glide.with(getActivity()).load(chat.getChatPhoto())
        .bitmapTransform(new CropCircleTransformation(getActivity()))
        .into(new SimpleTarget<GlideDrawable>() {
          @Override public void onResourceReady(GlideDrawable glideDrawable,
              GlideAnimation<? super GlideDrawable> glideAnimation) {
            toolbar.setLogo(glideDrawable);
          }
        });

    toolbar.setTitle(chat.getChatName());
    toolbar.setTitleMarginStart(DisplayUtil.dpToPx(32));
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

  @Override protected void onDestroy() {
    super.onDestroy();
    Timber.d("onDestroy()");
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override public void onAddAttachments() {
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

  @Override public boolean onSubmit(CharSequence charSequence) {
    ChatMessage message = new ChatMessageBuilder().setSenderId(oneId)
        .setMessage(charSequence.toString())
        .createChatMessage();

    if (adapter.getItemCount() == 0) {
      createChatIfNotExits(message);
    } else {
      chatRepository.sendMessage(chatId, message);
    }
    return true;
  }

  private void createChatIfNotExits(ChatMessage message) {
    Disposable d =
        chatRepository.createChat(oneId, oneUsername, oneAvatar, otherId, otherUsername,
            otherAvatar)
            .doOnSuccess(chat -> {
              chatId = chat.getChatId();
              setChatToolbarInfo(chat);
            })
            .subscribe(chat -> chatRepository.sendMessage(chat.getChatId(), message), Timber::e);

    disposable.add(d);
  }

  @Override public void onSelectionChanged(int count) {

  }

  @Override public void onLoadMore(int page, int totalItemCount) {

  }

  @Override public void onMessageClick(DefaultMessage message) {
    if (!TextUtils.isEmpty(message.getImageUrl())) {
      String biggerImageUrl = message.getImageUrl().replace("s150", "s800");
      getRouter().pushController(
          RouterTransaction.with(FullscreenPhotoController.create(biggerImageUrl))
              .pushChangeHandler(new FadeChangeHandler())
              .popChangeHandler(new FadeChangeHandler()));
    }
  }

  private void setupRecyclerView() {
    adapter = new MessagesListAdapter<>(getArgs().getString(KEY_ONE_ID),
        (imageView, url) -> {
          int size = DisplayUtil.dpToPx(200);
          imageView.getLayoutParams().height = size;
          imageView.getLayoutParams().width = size;
          imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
          Glide.with(getActivity()).load(url).into(imageView);
        });
    //adapter.enableSelectionMode(this);
    adapter.setLoadMoreListener(this);
    adapter.setOnMessageClickListener(this);
    messagesList.setHasFixedSize(true);
    messagesList.setAdapter(adapter);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    /*Disposable d = chatRepository.getLastSeen(otherId)
        .doOnNext(snapshot -> Timber.d("Snap: %s", snapshot))
        .map(snapshot -> snapshot.getValue(String.class))
        .subscribe(lastSeen -> toolbar.setSubtitle(lastSeen));
    disposable.add(d);*/

    toolbar.setNavigationOnClickListener(v -> {
      KeyboardUtil.hideKeyboard(getView());
      getRouter().popToTag(ChatListController.class.getName());
    });
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
      uploadMedia(uri);
    }
  }

  private void uploadMedia(Uri uri) {
    List<File> files = Collections.singletonList(new File(uri.getPath()));
    Disposable d = UploadManager.INSTANCE.upload(oneId, files, UploadManager.MediaOrigin.CHAT)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(response -> response.body().string())
        .map(json -> new Moshi.Builder().build().adapter(UploadResponse.class).fromJson(json))
        .map(response -> response.getItems().get(0).getSizes().get(1).getUrl())
        .subscribe(this::sendMessage);

    disposable.add(d);
  }

  private void sendMessage(String mediaIdUrl) {
    ChatMessage message = new ChatMessageBuilder().setSenderId(oneId)
        .setMessage(mediaIdUrl)
        .setAttachment(mediaIdUrl)
        .createChatMessage();

    if (adapter.getItemCount() == 0) {
      createChatIfNotExits(message);
    } else {
      chatRepository.sendMessage(chatId, message);
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

  private Observable<DefaultMessage> getMessagesObservable(String chatId) {
    return chatRepository.getMessages(chatId)
        .filter(e -> e.getDataSnapshot() != null)
        .map(event -> event.getDataSnapshot().getValue(ChatMessage.class))
        .map(DefaultMessage::new);
  }
}
