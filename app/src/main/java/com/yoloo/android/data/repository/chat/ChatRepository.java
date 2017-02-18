package com.yoloo.android.data.repository.chat;

import com.annimon.stream.Stream;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yoloo.android.data.model.firebase.Chat;
import com.yoloo.android.data.model.firebase.ChatMessage;
import com.yoloo.android.data.model.firebase.ChatUser;
import com.yoloo.android.rxfirebase.FirebaseChildEvent;
import com.yoloo.android.rxfirebase.RxFirebaseDatabase;
import com.yoloo.android.util.ReadMoreUtil;
import io.reactivex.Observable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

  private static ChatRepository instance;

  private DatabaseReference rootRef;
  private RxFirebaseDatabase rxFirebaseDb;

  private ChatRepository() {
    rootRef = FirebaseDatabase.getInstance().getReference();
    rxFirebaseDb = RxFirebaseDatabase.getInstance();
  }

  public static ChatRepository getInstance() {
    if (instance == null) {
      instance = new ChatRepository();
    }
    return instance;
  }

  public Observable<ChatMessage> getMessagesByChatId(String chatId) {
    return rxFirebaseDb.observeSingleValue(rootRef.child("messages/" + chatId))
        .map(dataSnapshot -> dataSnapshot.getValue(ChatMessage.class));
  }

  public void addChat(Chat chat, ChatUser from, List<ChatUser> toList) {
    final String chatId = rootRef.push().getKey();

    Map<String, Object> update = new HashMap<>();

    chat.setId(chatId);
    // chats/$chatId/meta
    update.put("chats/" + chatId + "/meta", chat);

    // members/$chatId/$userId
    Stream.concat(Stream.of(from), Stream.of(toList))
        .forEach(u -> update.put("members/" + chatId + "/" + u.getUserId(), u.getUserRole()));

    // users/$userId/$chatId
    Stream.concat(Stream.of(from), Stream.of(toList))
        .forEach(
            u -> update.put("users/" + u.getUserId() + "/" + "chats/" + chatId, u.getUserRole()));

    rootRef.updateChildren(update);
  }

  public void addMessage(Chat chat, ChatMessage message) {
    Map<String, Object> update = new HashMap<>();

    update.put("messages/" + chat.getId(), message);

    update.put("chats/" + message.getChatId(), chat
        .setLastMessage(ReadMoreUtil.readMoreContent(message, 40))
        .increaseMissedMessages()
        .updateTimestamp());

    rootRef.updateChildren(update);
  }

  public void deleteConversation(String conversationId) {
    Map<String, Object> childDelete = new HashMap<>();
    childDelete.put("members/" + conversationId, null);
    childDelete.put("conversations/" + conversationId, null);
    childDelete.put("messages/" + conversationId, null);

    rootRef.updateChildren(childDelete);
  }

  public Observable<FirebaseChildEvent> listChatsByUserId(String userId) {
    return rxFirebaseDb
        .observeSingleValue(rootRef.child("users/" + userId + "/chats"))
        .flatMapIterable(DataSnapshot::getChildren)
        .map(DataSnapshot::getKey)
        .concatMap(chatId -> rxFirebaseDb.observeChildEvent(rootRef.child("chats/" + chatId)));
  }
}
