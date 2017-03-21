package com.yoloo.android.feature.chat.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.chatkit.commons.ImageLoader;
import com.yoloo.android.chatkit.messages.MessageInput;
import com.yoloo.android.chatkit.messages.MessagesList;
import com.yoloo.android.chatkit.messages.MessagesListAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.chat.DialogMessage;
import com.yoloo.android.data.model.chat.NormalDialog;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.model.firebase.ChatMessage;
import com.yoloo.android.data.model.firebase.ChatUser;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import com.yoloo.android.util.KeyboardUtil;

import butterknife.BindView;

public class DialogController extends MvpController<DialogView, DialogPresenter>
    implements DialogView, MessagesListAdapter.SelectionListener {

  private static final String KEY_DIALOG = "DIALOG";

  @BindView(R.id.toolbar_dialog) Toolbar toolbar;
  @BindView(R.id.rv_dialog) MessagesList messagesList;
  @BindView(R.id.input_dialog) MessageInput inputView;

  private AccountRealm me;

  private NormalDialog dialog;
  private MessagesListAdapter<DialogMessage> adapter;
  private int selectionCount;

  public DialogController(@Nullable Bundle args) {
    super(args);
    setHasOptionsMenu(true);
  }

  public static DialogController create(@NonNull NormalDialog dialog) {
    final Bundle bundle = new BundleBuilder().putParcelable(KEY_DIALOG, dialog).build();

    return new DialogController(bundle);
  }

  @Override protected View inflateView(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_dialog, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    dialog = getArgs().getParcelable(KEY_DIALOG);

    setupToolbar();
    setupRecyclerView();

    inputView.setInputListener(input -> {
      ChatUser chatUser = new ChatUser(me.getId(), me.getAvatarUrl(), me.getUsername(), 1);
      ChatMessage chatMessage = new ChatMessage(chatUser, dialog.getId(), input.toString());

      getPresenter().sendMessage(chatMessage);
      return true;
    });
  }

  @Override protected void onAttach(@NonNull View view) {
    if (!isChatBot()) {
      getPresenter().getMessages(dialog.getId());
      ControllerUtil.preventDefaultBackPressAction(view, () -> {
        deleteDialogHistoryIsEmpty();
        getRouter().handleBack();
      });
    }
  }

  @Override public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getActivity().getCurrentFocus());
    return super.handleBack();
  }

  @Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_dialog, menu);
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      if (!isChatBot()) {
        // User didn't type a message, so remove chat.
        deleteDialogHistoryIsEmpty();
      }
      getRouter().handleBack();
      return true;
    } else if (itemId == R.id.action_group_info) {

    }
    return super.onOptionsItemSelected(item);
  }

  @NonNull @Override public DialogPresenter createPresenter() {
    return new DialogPresenter(
        ChatRepository.getInstance(),
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()));
  }

  @Override public void onMeLoaded(AccountRealm me) {
    this.me = me;
    adapter.setSenderId(me.getId());
  }

  @Override public void onMessageAdded(ChatMessage message) {
    adapter.addToStart(new DialogMessage(message), true);
  }

  @Override public void onMessageChanged(ChatMessage message) {
    adapter.update(new DialogMessage(message));
  }

  @Override public void onMessageRemoved(ChatMessage message) {

  }

  @Override public void onError(Throwable throwable) {

  }

  @Override public void onSelectionChanged(int count) {
    selectionCount = count;
  }

  private void setupRecyclerView() {
    ImageLoader loader = (imageView, url) -> Glide.with(getActivity()).load(url).into(imageView);

    adapter = new MessagesListAdapter<>(loader);
    adapter.enableSelectionMode(this);
    messagesList.setAdapter(adapter);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);

      toolbar.post(() -> ab.setTitle(dialog.getDialogName()));
    }
  }

  private void deleteDialogHistoryIsEmpty() {
    if (adapter.getItemCount() == 0) {
      getPresenter().deleteDialog(new Chat(dialog));
    }
  }

  private boolean isChatBot() {
    return dialog.getId().equals("dialog.chatbot");
  }
}
