package com.yoloo.android.feature.chat.chatlist;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnClick;
import com.annimon.stream.Stream;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.yoloo.android.R;
import com.yoloo.android.data.chat.LastMessage;
import com.yoloo.android.data.chat.SummaryDialog;
import com.yoloo.android.data.chat.firebase.Chat;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.chat.chat.ChatController;
import com.yoloo.android.feature.chat.createchat.CreateChatController;
import com.yoloo.android.rxfirebase.FirebaseChildEvent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import timber.log.Timber;

public class ChatListController extends BaseController
    implements DialogsListAdapter.OnDialogClickListener<SummaryDialog>,
    DialogsListAdapter.OnDialogLongClickListener<SummaryDialog> {

  @BindView(R.id.dialogsList) DialogsList dialogsList;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.empty_view) ViewGroup emptyView;

  private DialogsListAdapter<SummaryDialog> adapter;

  private ChatRepository chatRepository;

  private CompositeDisposable disposable;

  private AlertDialog alertDialog;

  public static ChatListController create() {
    return new ChatListController();
  }

  private static String findOtherUserId(@NonNull String chatId, @NonNull String currentUserId) {
    return Stream.of(chatId.substring(5).split("_"))
        .filter(value -> !value.equals(currentUserId))
        .single();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_chat_list, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    disposable = new CompositeDisposable();

    setupToolbar();
    setRecyclerView();

    coordinateEmptyView(true);

    fetchChats();
  }

  @Override protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (alertDialog != null && alertDialog.isShowing()) {
      alertDialog.dismiss();
    }
  }

  private void fetchChats() {
    chatRepository = ChatRepository.getInstance();
    UserRepository userRepository = UserRepositoryProvider.getRepository();

    Disposable d = userRepository.getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(account -> {
          Disposable d2 = chatRepository.observeChats(account.getId())
              .subscribe(e -> handleEvent(account, e), Timber::e);
          disposable.add(d2);
        }, Timber::e);
    disposable.add(d);
  }

  private void handleEvent(AccountRealm account, FirebaseChildEvent e) {
    switch (e.getEventType()) {
      case ADDED:
        coordinateEmptyView(false);
        adapter.addItem(getSummaryDialog(account, e.getDataSnapshot()));
        break;
      case CHANGED:
        coordinateEmptyView(false);
        adapter.updateItemById(getSummaryDialog(account, e.getDataSnapshot()));
        break;
      case REMOVED:
        adapter.deleteById(e.getDataSnapshot().getKey());
        coordinateEmptyView(true);
        break;
      case MOVED:
        break;
    }
  }

  @NonNull private SummaryDialog getSummaryDialog(AccountRealm account, DataSnapshot snapshot) {
    Chat chat = snapshot.getValue(Chat.class);

    List<SummaryDialog.SummaryDialogUser> users = new ArrayList<>(2);

    users.add(new SummaryDialog.SummaryDialogUser(account));
    users.add(new SummaryDialog.SummaryDialogUser(
        findOtherUserId(chat.getChatId(), account.getId()), chat.getChatName(),
        chat.getChatPhoto()));

    long lastMessageTs = chat.getLastMessageTs() == null
        ? new Date().getTime()
        : (long) chat.getLastMessageTs();

    return new SummaryDialog(chat, users, new LastMessage(chat.getLastMessage(),
        lastMessageTs));
  }

  private void setRecyclerView() {
    adapter = new DialogsListAdapter<>(
        (imageView, url) -> Glide.with(getActivity()).load(url).into(imageView));

    adapter.setOnDialogClickListener(this);
    adapter.setOnDialogLongClickListener(this);

    dialogsList.setHasFixedSize(true);
    dialogsList.setAdapter(adapter);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Timber.d("onDestroy()");
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override public void onDialogClick(SummaryDialog dialog) {
    IUser oneUser = dialog.getUsers().get(0);
    AccountRealm one = new AccountRealm()
        .setId(oneUser.getId())
        .setAvatarUrl(oneUser.getAvatar())
        .setUsername(oneUser.getName());

    IUser otherUser = dialog.getUsers().get(1);
    AccountRealm other = new AccountRealm()
        .setId(otherUser.getId())
        .setAvatarUrl(otherUser.getAvatar())
        .setUsername(otherUser.getName());

    getRouter().pushController(RouterTransaction
        .with(ChatController.create(one, other))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override public void onDialogLongClick(SummaryDialog dialog) {
    alertDialog = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.chat_list_dialog_title)
        .setItems(R.array.chat_list_options, (d, which) -> {
          if (which == 0) {
            chatRepository.deleteChat(dialog.getId());
          }
        }).show();
  }

  @OnClick(R.id.fab_create_chat) void openCreateChatScreen() {
    getRouter().pushController(RouterTransaction
        .with(CreateChatController.create())
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle(R.string.chat_list_title);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void coordinateEmptyView(boolean show) {
    emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    dialogsList.setVisibility(show ? View.GONE : View.VISIBLE);
  }
}
