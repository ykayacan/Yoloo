package com.yoloo.android.feature.chat.conversationlist;

import com.yoloo.android.data.model.AccountRealm;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.model.firebase.ChatUser;
import com.yoloo.android.data.repository.chat.ChatRepository;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.Collections;
import java.util.UUID;

class ConversationListPresenter extends MvpPresenter<ConversationListView> {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;

  ConversationListPresenter(UserRepository userRepository, ChatRepository chatRepository) {
    this.userRepository = userRepository;
    this.chatRepository = chatRepository;
  }

  @Override public void onAttachView(ConversationListView view) {
    super.onAttachView(view);
    //createChat();
    loadChats();
  }

  private void loadChats() {
    Disposable d = userRepository.getLocalMe().toObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .map(AccountRealm::getId)
        .flatMap(chatRepository::listChatsByUserId)
        .subscribe(event -> {
          Chat chat = event.getDataSnapshot().getValue(Chat.class);
          switch (event.getEventType()) {
            case ADDED:
              getView().onChatAdded(chat);
              break;
            case CHANGED:
              getView().onChatChanged(chat);
              break;
            case REMOVED:
              getView().onChatRemoved(chat);
              break;
            case MOVED:
              break;
          }
        }, throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }

  private void createChat() {
    Chat chat = new Chat();
    chat.setLastMessage("krialix: No problem");
    chat.setCoverImageUrl("imageUrl");
    chat.setTitle("Chat title");

    ChatUser from = new ChatUser("a1", ChatUser.ROLE_ADMIN);
    ChatUser to = new ChatUser(UUID.randomUUID().toString(), ChatUser.ROLE_USER);

    chatRepository.addChat(chat, from, Collections.singletonList(to));
  }
}
