package com.yoloo.android.feature.chat.dialoglist;

/*class DialogListPresenter extends MvpPresenter<DialogListView> {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;

  DialogListPresenter(UserRepository userRepository, ChatRepository chatRepository) {
    this.userRepository = userRepository;
    this.chatRepository = chatRepository;
  }

  @Override public void onAttachView(DialogListView view) {
    super.onAttachView(view);
    loadDialogs();
  }

  private void loadDialogs() {
    Disposable d = userRepository.getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess(me -> getView().onMeLoaded(me))
        .map(AccountRealm::getId)
        .flatMapObservable(chatRepository::listDialogsByUserId)
        .subscribe(event -> {
          Chat chat = event.getDataSnapshot().getValue(Chat.class);
          switch (event.getEventType()) {
            case ADDED:
              getView().onDialogAdded(chat);
              break;
            case CHANGED:
              getView().onDialogChanged(chat);
              break;
            case REMOVED:
              getView().onDialogRemoved(chat);
              break;
            case MOVED:
              break;
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void deleteDialog(@Nonnull Chat chat) {
    chatRepository.deleteDialog(chat);
  }

  void exitGroup(@Nonnull String dialogId, @Nonnull String userIdToRemove) {
    Timber.d("DialogId: %s, userId: %s", dialogId, userIdToRemove);
    //chatRepository.exitGroup(dialogId, userIdToRemove);
  }
}*/
