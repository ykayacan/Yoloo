package com.yoloo.android.feature.chat.dialoglist;

/*public class DialogListController extends MvpController<DialogListView, DialogListPresenter>
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
    return new DialogListPresenter(UserRepositoryProvider.getRepository(),
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
    ImageLoader imageLoader =
        (imageView, url) -> Glide.with(getActivity()).load(url).into(imageView);

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

    new AlertDialog.Builder(getActivity()).setItems(
        isGroup ? groupDialogOptions : normalDialogOptions, (dialog, which) -> {
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
        }).show();
  }
}*/
