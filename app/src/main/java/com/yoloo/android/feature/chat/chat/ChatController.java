package com.yoloo.android.feature.chat.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.yoloo.android.R;
import com.yoloo.android.data.chat.Dialog;
import com.yoloo.android.data.chat.Message;
import com.yoloo.android.data.chat.User;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.chat.chatlist.ChatListController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.KeyboardUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ChatController extends BaseController
    implements MessageInput.InputListener, MessageInput.AttachmentsListener,
    MessagesListAdapter.SelectionListener, MessagesListAdapter.OnLoadMoreListener {

  private static final String KEY_DIALOG_ID = "DIALOG_ID";
  private static final String KEY_SENDER_ID = "SENDER_ID";

  @BindView(R.id.messagesList) MessagesList messagesList;
  @BindView(R.id.input) MessageInput messageInput;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private Dialog dialog;
  private String senderId;

  private Disposable disposable;

  private MessagesListAdapter<Message> messagesListAdapter;

  private ChatRepository chatRepository;

  public ChatController(Bundle args) {
    super(args);
  }

  public static ChatController create(@NonNull String senderId, @NonNull Dialog dialog) {
    final Bundle bundle = new BundleBuilder()
        .putString(KEY_SENDER_ID, senderId)
        .putParcelable(KEY_DIALOG_ID, dialog)
        .build();

    return new ChatController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_chat, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    dialog = getArgs().getParcelable(KEY_DIALOG_ID);
    senderId = getArgs().getString(KEY_SENDER_ID);

    setupToolbar();
    setupRecyclerview();

    ControllerUtil.preventDefaultBackPressAction(view, () -> {
      KeyboardUtil.hideKeyboard(view);
      deleteDialogIfNoMessage();
      getRouter().popToTag(ChatListController.TAG);
    });

    messageInput.setInputListener(this);

    chatRepository = ChatRepository.getInstance();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    disposable = chatRepository
        .getMessagesByDialogId(Stream
            .of(dialog.getMembers().keySet())
            .filter(value -> !value.equals(senderId))
            .single(), dialog.getId())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(event -> {
          Message message = event.getDataSnapshot().getValue(Message.class);

          switch (event.getEventType()) {
            case ADDED:
              messagesListAdapter.addToStart(message, true);
              break;
            case CHANGED:
              break;
            case REMOVED:
              break;
            case MOVED:
              break;
          }
        });
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override
  public void onAddAttachments() {

  }

  @Override
  public boolean onSubmit(CharSequence charSequence) {
    if (messagesListAdapter.getItemCount() == 0) {
      chatRepository.addDialogToTargetUser(dialog);
    }

    final User currentUser =
        Stream.of(dialog.getUsers()).filter(value -> value.getId().equals(senderId)).single();

    chatRepository.sendMessage(dialog, senderId, new Message(charSequence.toString(), currentUser));
    return true;
  }

  @Override
  public void onSelectionChanged(int count) {

  }

  @Override
  public void onLoadMore(int page, int totalItemCount) {

  }

  private void setupRecyclerview() {
    messagesListAdapter = new MessagesListAdapter<>(senderId,
        (imageView, url) -> Glide.with(getActivity()).load(url).into(imageView));
    //messagesListAdapter.enableSelectionMode(this);
    messagesListAdapter.setLoadMoreListener(this);
    messagesList.setHasFixedSize(true);
    messagesList.setAdapter(messagesListAdapter);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    getSupportActionBar().setTitle(dialog.getDialogName());
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    toolbar.setNavigationOnClickListener(v -> {
      KeyboardUtil.hideKeyboard(getView());
      deleteDialogIfNoMessage();
      getRouter().popToTag(ChatListController.TAG);
    });
  }

  private void deleteDialogIfNoMessage() {
    if (messagesListAdapter.isEmpty()) {
      chatRepository.deleteDialog(dialog);
    }
  }
}
