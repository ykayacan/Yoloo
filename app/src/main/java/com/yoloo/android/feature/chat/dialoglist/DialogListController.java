package com.yoloo.android.feature.chat.dialoglist;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.chatkit.commons.ImageLoader;
import com.yoloo.android.chatkit.commons.models.IDialog;
import com.yoloo.android.chatkit.dialogs.DialogsList;
import com.yoloo.android.chatkit.dialogs.DialogsListAdapter;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.chat.NormalDialog;
import com.yoloo.android.data.model.chat.chatbot.ChatBotDialog;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.datasource.UserDiskDataStore;
import com.yoloo.android.data.repository.user.datasource.UserRemoteDataStore;
import com.yoloo.android.feature.chat.createdialog.CreateDialogController;
import com.yoloo.android.feature.chat.dialog.DialogController;
import com.yoloo.android.framework.MvpController;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class DialogListController extends MvpController<DialogListView, DialogListPresenter>
    implements DialogListView {

  @BindView(R.id.chat_list) DialogsList dialogsList;
  @BindView(R.id.toolbar_chat) Toolbar toolbar;

  @BindArray(R.array.action_dialog_list_group_options_dialog) CharSequence[] groupDialogOptions;
  @BindArray(R.array.action_dialog_list_normal_options_dialog) CharSequence[] normalDialogOptions;

  private DialogsListAdapter<NormalDialog> adapter;

  private AccountRealm me;

  public DialogListController() {
  }

  public static DialogListController create() {
    return new DialogListController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_dialog_list, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupToolbar();
    setHasOptionsMenu(true);
    setupRecyclerView();
    setupChatBot();
  }

  private void setupChatBot() {
    adapter.addItem(0, new ChatBotDialog());
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onMeLoaded(AccountRealm me) {
    this.me = me;
  }

  @Override public void onDialogAdded(Chat chat) {
    adapter.addItem(1, NormalDialog.from(chat));
    dialogsList.smoothScrollToPosition(1);
  }

  @Override public void onDialogChanged(Chat chat) {
    Timber.d("onDialogChanged(): %s", chat);
    adapter.updateItemById(NormalDialog.from(chat));
  }

  @Override public void onDialogRemoved(Chat chat) {
    adapter.deleteById(chat.getId());
  }

  @Override public void onError(Throwable throwable) {
    Timber.e(throwable);
  }

  @NonNull @Override public DialogListPresenter createPresenter() {
    return new DialogListPresenter(
        UserRepository.getInstance(
            UserRemoteDataStore.getInstance(),
            UserDiskDataStore.getInstance()),
        ChatRepository.getInstance());
  }

  @OnClick(R.id.fab_create_dialog) void createDialog() {
    Controller controller = CreateDialogController.create(me.getId());
    RouterTransaction transaction = RouterTransaction.with(controller)
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler());

    setRetainViewMode(RetainViewMode.RELEASE_DETACH);
    getRouter().pushController(transaction);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPost back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_toolbar_conversationlist_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupRecyclerView() {
    ImageLoader imageLoader = (imageView, url) ->
        Glide.with(getActivity()).load(url).into(imageView);

    adapter = new DialogsListAdapter<>(imageLoader);
    dialogsList.setAdapter(adapter);

    adapter.setOnDialogClickListener(dialog -> {
      Controller controller = DialogController.create(dialog);
      RouterTransaction transaction = RouterTransaction.with(controller)
          .pushChangeHandler(new VerticalChangeHandler())
          .popChangeHandler(new VerticalChangeHandler());

      setRetainViewMode(RetainViewMode.RETAIN_DETACH);
      getRouter().pushController(transaction);
    });

    adapter.setOnDialogLongClickListener(this::showDeleteDialog);
  }

  private void showDeleteDialog(IDialog normalDialog) {
    boolean isGroup = normalDialog.getUsers().size() > 2;

    new AlertDialog.Builder(getActivity())
        .setItems(isGroup ? groupDialogOptions : normalDialogOptions, (dialog, which) -> {
          if (isGroup) {
            if (which == 0) {

            } else if (which == 1) {
              getPresenter().exitGroup(normalDialog.getId(), me.getId());
            }
          } else {
            if (which == 0) {
              getPresenter().exitGroup(normalDialog.getId(), me.getId());
            }
          }
        })
        .show();
  }
}
