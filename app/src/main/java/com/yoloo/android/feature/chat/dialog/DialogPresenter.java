package com.yoloo.android.feature.chat.dialog;

import com.google.firebase.database.DataSnapshot;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.model.firebase.ChatMessage;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

class DialogPresenter extends MvpPresenter<DialogView> {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;

  DialogPresenter(ChatRepository chatRepository, UserRepository userRepository) {
    this.chatRepository = chatRepository;
    this.userRepository = userRepository;
  }

  @Override public void onAttachView(DialogView view) {
    super.onAttachView(view);
    loadMe();
  }

  private void loadMe() {
    Disposable d = userRepository.getLocalMe()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(me -> getView().onMeLoaded(me), throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void getMessages(@Nonnull String dialogId) {
    Disposable d = chatRepository.getMessagesByDialogId(dialogId)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(event -> {
          DataSnapshot snapshot = event.getDataSnapshot();
          ChatMessage message = snapshot.getValue(ChatMessage.class);
          message.setId(snapshot.getKey());
          message.setDialogId(dialogId);
          switch (event.getEventType()) {
            case ADDED:
              getView().onMessageAdded(message);
              break;
            case CHANGED:
              getView().onMessageChanged(message);
              break;
            case REMOVED:
              getView().onMessageRemoved(message);
              break;
            case MOVED:
              break;
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  void sendMessage(@Nonnull ChatMessage message) {
    chatRepository.addMessage(message);
  }

  void removeMessage(@Nonnull ChatMessage message) {
    chatRepository.deleteMessage(message);
  }

  void deleteDialog(@Nonnull Chat chat) {
    chatRepository.deleteDialog(chat);
  }
}
