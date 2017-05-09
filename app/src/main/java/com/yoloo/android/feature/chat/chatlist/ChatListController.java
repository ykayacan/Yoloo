package com.yoloo.android.feature.chat.chatlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnClick;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.yoloo.android.R;
import com.yoloo.android.data.chat.Dialog;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.chat.chat.ChatController;
import com.yoloo.android.feature.chat.createchat.CreateChatController;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class ChatListController extends BaseController
    implements DialogsListAdapter.OnDialogClickListener<Dialog>,
    DialogsListAdapter.OnDialogLongClickListener<Dialog> {

  public static final String TAG = ChatListController.class.getSimpleName();

  private static final String KEY_USER_ID = "USER_ID";

  @BindView(R.id.dialogsList) DialogsList dialogsList;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private DialogsListAdapter<Dialog> dialogsListAdapter;
  private String userId;

  private ChatRepository chatRepository;

  private Disposable disposable;

  public ChatListController(Bundle args) {
    super(args);
  }

  public static ChatListController create(@NonNull String userId) {
    Bundle bundle = new Bundle();
    bundle.putString(KEY_USER_ID, userId);

    return new ChatListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_chat_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    userId = getArgs().getString(KEY_USER_ID);

    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(R.string.chat_list_title);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    dialogsListAdapter = new DialogsListAdapter<>(
        (imageView, url) -> Glide.with(getActivity()).load(url).into(imageView));

    dialogsListAdapter.setOnDialogClickListener(this);
    dialogsListAdapter.setOnDialogLongClickListener(this);

    dialogsList.setAdapter(dialogsListAdapter);

    chatRepository = ChatRepository.getInstance();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    dialogsListAdapter.clear();

    disposable = chatRepository
        .getDialogsByUserId(userId)
        .observeOn(AndroidSchedulers.mainThread())
        .distinct()
        .subscribe(event -> {
          Dialog dialog = event.getDataSnapshot().getValue(Dialog.class);
          dialog = dialog.withCurrentUser(userId);
          Log.i(TAG, "onViewBound: Dialog:" + dialog);

          switch (event.getEventType()) {
            case ADDED:
              dialogsListAdapter.addItem(dialog);
              break;
            case CHANGED:
              dialogsListAdapter.updateItemById(dialog);
              break;
            case REMOVED:
              dialogsListAdapter.deleteById(dialog.getId());
              break;
            case MOVED:
              break;
          }
        }, Timber::e);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override
  public void onDialogClick(Dialog dialog) {
    getRouter().pushController(RouterTransaction
        .with(ChatController.create(userId, dialog))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override
  public void onDialogLongClick(Dialog dialog) {
    chatRepository.deleteDialog(dialog);
  }

  @OnClick(R.id.fab_create_chat)
  void openCreateChatScreen() {
    getRouter().pushController(RouterTransaction
        .with(CreateChatController.create())
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }
}
