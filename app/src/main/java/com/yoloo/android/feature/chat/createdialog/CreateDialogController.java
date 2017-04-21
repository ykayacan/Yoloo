package com.yoloo.android.feature.chat.createdialog;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.airbnb.epoxy.EpoxyModel;
import com.yoloo.android.R;
import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.EndlessRecyclerViewScrollListener;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.SelectableAdapter;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.BundleBuilder;
import io.reactivex.subjects.PublishSubject;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateDialogController extends MvpController<CreateDialogView, CreateDialogPresenter>
    implements CreateDialogView, OnItemClickListener<AccountRealm>,
    SelectableAdapter.OnSelectionListener, EndlessRecyclerViewScrollListener.OnLoadMoreListener {

  private static final String KEY_USER_ID = "USER_ID";

  private final PublishSubject<String> searchSubject = PublishSubject.create();

  @BindView(R.id.searchview_createdialog) SearchView searchView;
  @BindView(R.id.search_edit_frame) Toolbar toolbar;
  @BindView(R.id.rv_createdialog) RecyclerView rvCreateDialog;

  @BindView(R.id.tv_createdialog_start_conversation) TextView tvStartConversation;

  @BindString(R.string.label_create_dialog_toolbar_title) String createDialogTitleString;

  private CreateDialogContactAdapter adapter;

  private AccountRealm me;

  private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;

  public CreateDialogController(@Nullable Bundle args) {
    super(args);
    setHasOptionsMenu(true);
  }

  public static CreateDialogController create(String userId) {
    final Bundle bundle = new BundleBuilder().putString(KEY_USER_ID, userId).build();

    return new CreateDialogController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_createdialog, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    setupToolbar();
    setupRecyclerView();

    SearchManager searchManager =
        (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        searchSubject.onNext(newText);
        return false;
      }
    });
  }

  @Override
  protected void onAttach(@NonNull View view) {
    searchSubject
        .filter(s -> !s.isEmpty())
        .debounce(400, TimeUnit.MILLISECONDS)
        .subscribe(query -> {
          if (TextUtils.isEmpty(query)) {
            getPresenter().loadFollowers(me.getId());
          } else {
            getPresenter().searchUsers(query);
          }
        });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<AccountRealm> value) {
    adapter.addContacts(value);
  }

  @Override
  public void onError(Throwable e) {

  }

  @Override
  public void onEmpty() {

  }

  @Override
  public void onLoadMore() {

  }

  @NonNull
  @Override
  public CreateDialogPresenter createPresenter() {
    return new CreateDialogPresenter(UserRepositoryProvider.getRepository(),
        ChatRepository.getInstance());
  }

  @Override
  public void onMeLoaded(AccountRealm me) {
    this.me = me;
  }

  @Override
  public void onUsersLoaded(List<AccountRealm> accounts) {
    adapter.clear();
    adapter.addContacts(accounts);
  }

  @Override
  public void onItemClick(View v, EpoxyModel<?> model, AccountRealm item) {
   /* Map<String, ChatUser> chatUsers = new HashMap<>();
    final ChatUser chatUser = new ChatUser(item, 1);
    final ChatUser admin = new ChatUser(me, 3);

    chatUsers.put(item.getId(), chatUser);
    chatUsers.put(me.getId(), admin);

    Chat chat = new Chat().setName(item.getUsername())
        .setChatPhotoUrl("https://storage.googleapis.com/yoloo-151719.appspot.com/"
            + "system-default/empty_user_avatar.webp")
        .setLastSenderId(me.getId())
        .setMembers(chatUsers);

    final Chat saved = getPresenter().createDialog(chat);

    Controller controller = DialogController.create(NormalDialog.from(saved));
    RouterTransaction transaction = RouterTransaction.with(controller)
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler());

    getRouter().pushController(transaction);
    getRouter().popController(this);*/
  }

  @Override
  public void onSelect(EpoxyModel<?> model, boolean selected) {
    final int selectedItems = adapter.getSelectedItemCount();
    tvStartConversation.setVisibility(selectedItems == 0 ? View.GONE : View.VISIBLE);
    final String title =
        selectedItems == 0 ? createDialogTitleString : String.valueOf(selectedItems);
    getSupportActionBar().setTitle(title);
  }

  @OnClick(R.id.tv_createdialog_start_conversation)
  void createDialog() {
    /*Map<String, ChatUser> chatUsers = Stream.of(adapter.getSelectedItems())
        .select(CreateDialogContactAdapter.CreateDialogContactModel.class)
        .map(CreateDialogContactAdapter.CreateDialogContactModel::getAccount)
        .map(user -> new ChatUser(user, 1))
        .collect(Collectors.toMap(ChatUser::getId));

    final ChatUser admin = new ChatUser(me, 3);
    chatUsers.put(me.getId(), admin);

    final String chatName = Stream.of(chatUsers)
        .map(Map.Entry::getValue)
        .map(ChatUser::getName)
        .collect(Collectors.joining(", "));

    Chat chat = new Chat().setName(chatName)
        .setChatPhotoUrl("https://storage.googleapis.com/yoloo-151719.appspot.com/"
            + "system-default/empty_user_avatar.webp")
        .setLastSenderId(me.getId())
        .setMembers(chatUsers);

    final Chat saved = getPresenter().createDialog(chat);

    Controller controller = DialogController.create(NormalDialog.from(saved));
    RouterTransaction transaction = RouterTransaction.with(controller)
        .pushChangeHandler(new VerticalChangeHandler())
        .popChangeHandler(new VerticalChangeHandler());

    getRouter().pushController(transaction);
    getRouter().popController(this);*/
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(createDialogTitleString);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupRecyclerView() {
    adapter = new CreateDialogContactAdapter(getActivity(), this);
    adapter.setOnSelectionListener(this);

    final LinearLayoutManager lm = new LinearLayoutManager(getActivity());

    rvCreateDialog.setLayoutManager(lm);

    final SlideInItemAnimator animator = new SlideInItemAnimator();
    animator.setSupportsChangeAnimations(false);
    rvCreateDialog.setItemAnimator(animator);
    rvCreateDialog.setHasFixedSize(true);
    rvCreateDialog.setAdapter(adapter);

    //endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(lm, this);
  }
}
