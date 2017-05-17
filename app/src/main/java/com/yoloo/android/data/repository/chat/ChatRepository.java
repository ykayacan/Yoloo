package com.yoloo.android.data.repository.chat;

import com.annimon.stream.Stream;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.yoloo.android.data.chat.firebase.Chat;
import com.yoloo.android.data.chat.firebase.ChatBuilder;
import com.yoloo.android.data.chat.firebase.ChatMessage;
import com.yoloo.android.data.chat.firebase.ChatMessageBuilder;
import com.yoloo.android.data.chat.firebase.ChatUser;
import com.yoloo.android.data.db.AccountRealm;
import com.yoloo.android.rxfirebase.FirebaseChildEvent;
import com.yoloo.android.rxfirebase.RxFirebaseDatabase;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class ChatRepository {

  private static ChatRepository instance;

  private DatabaseReference rootRef;
  private DatabaseReference usersRef;
  private DatabaseReference chatsRef;
  private DatabaseReference messagesRef;
  private DatabaseReference membersRef;

  private RxFirebaseDatabase rxFirebaseDb;

  private ChatRepository() {
    rxFirebaseDb = RxFirebaseDatabase.getInstance();

    rootRef = FirebaseDatabase.getInstance().getReference();
    chatsRef = rootRef.child("chats");
    membersRef = rootRef.child("members");
    messagesRef = rootRef.child("messages");
    usersRef = rootRef.child("users");
  }

  public static ChatRepository getInstance() {
    if (instance == null) {
      instance = new ChatRepository();
    }
    return instance;
  }

  public static String createOneToOneChatId(@Nonnull String oneId, @Nonnull String otherId) {
    if (oneId.equals(otherId)) {
      throw new IllegalArgumentException("Same user");
    }

    return "chat_" + (oneId.compareToIgnoreCase(otherId) < 0
        ? oneId + "_" + otherId
        : otherId + "_" + oneId);
  }

  private static String getLastMessage(@Nonnull String content) {
    return content.length() > 38 ? content.substring(0, 38).concat("...") : content;
  }

  public Single<Chat> getChatById(@Nonnull String oneId, @Nonnull String chatId) {
    return rxFirebaseDb
        .observeSingleValue(usersRef.child(oneId).child("chats").child(chatId))
        .map(snapshot -> snapshot.getValue(Chat.class))
        .singleOrError();
  }

  public Maybe<Chat> getChat(@Nonnull String oneId, @Nonnull String otherId) {
    return rxFirebaseDb
        .observeSingleValue(
            usersRef.child(oneId).child("chats").child(createOneToOneChatId(oneId, otherId)))
        .flatMapMaybe(snapshot -> {
          if (snapshot.exists()) {
            return Maybe.just(snapshot.getValue(Chat.class));
          }

          return Maybe.empty();
        }).singleElement();
  }

  public Single<Chat> createChat(@Nonnull AccountRealm one, @Nonnull AccountRealm other) {
    return createChat(one.getId(), one.getUsername(), one.getAvatarUrl(), other.getId(),
        other.getUsername(), other.getAvatarUrl());
  }

  public Single<Chat> createChat(
      @Nonnull String oneId,
      @Nonnull String oneUsername,
      @Nonnull String oneAvatar,
      @Nonnull String otherId,
      @Nonnull String otherUsername,
      @Nonnull String otherAvatar) {
    return Single.fromCallable(() -> {
      final String chatId = createOneToOneChatId(oneId, otherId);

      Chat chatOne = new ChatBuilder()
          .setChatId(chatId)
          .setChatName(otherUsername)
          .setChatPhoto(otherAvatar)
          .setCreatedByUserId(oneId)
          .setType(Chat.USER)
          .createChat();

      Chat chatOther = new ChatBuilder()
          .setChatId(chatId)
          .setChatName(oneUsername)
          .setChatPhoto(oneAvatar)
          .setCreatedByUserId(oneId)
          .setType(Chat.USER)
          .createChat();

      Map<String, Integer> members = new HashMap<>();
      members.put(oneId, ChatUser.ROLE_ADMIN);
      members.put(otherId, ChatUser.ROLE_MEMBER);

      Map<String, Object> update = new HashMap<>();
      update.put("users/" + oneId + "/chats/" + chatId, chatOne);
      update.put("users/" + oneId + "/lastSeen", ServerValue.TIMESTAMP);
      update.put("users/" + otherId + "/chats/" + chatId, chatOther);
      update.put("users/" + otherId + "/lastSeen", ServerValue.TIMESTAMP);
      update.put("members/" + chatId, members);

      rootRef.updateChildren(update);

      return chatOne;
    });
  }

  public void leftFromChat(@Nonnull String userId, @Nonnull String chatId) {
    rxFirebaseDb.observeSingleValue(membersRef.child(chatId))
        .map(DataSnapshot::getChildren)
        .flatMap(Observable::fromIterable)
        .filter(snapshot -> snapshot.getKey().equals(userId))
        .toList()
        .subscribe(snapshots -> {
          Map<String, Object> update = new HashMap<>();

          update.put("users/" + userId + "/chats/" + chatId, null);

          Stream.of(snapshots)
              .map(DataSnapshot::getKey)
              .forEach(id -> {
                // Update other users
                // Update user has left message
                update.put("users/" + id + "/chats/" + chatId + "/lastMessage",
                    "User has left conversation");
                // Update ts of user left action
                update.put("users/" + id + "/chats/" + chatId + "/lastMessageTs",
                    ServerValue.TIMESTAMP);
              });

          rootRef.updateChildren(update);
        });
  }

  public void deleteChat(@Nonnull String chatId) {
    rxFirebaseDb.observeSingleValue(membersRef.child(chatId))
        .map(DataSnapshot::getChildren)
        .subscribe(snapshots -> {
          Map<String, Object> delete = new HashMap<>();

          Stream.of(snapshots)
              .map(DataSnapshot::getKey)
              .forEach(userId -> delete.put("users/" + userId + "/chats/" + chatId, null));

          // Delete all members
          delete.put("members/" + chatId, null);
          // Delete all messages
          delete.put("messages/" + chatId, null);

          rootRef.updateChildren(delete);
        });
  }

  public void sendMessage(@Nonnull String chatId, @Nonnull ChatMessage chatMessage) {
    rxFirebaseDb.observeSingleValue(membersRef.child(chatId))
        .map(DataSnapshot::getChildren)
        .subscribe(snapshots -> {
          Map<String, Object> update = new HashMap<>();

          ChatMessage message = new ChatMessageBuilder()
              .setMessageId(rootRef.push().getKey())
              .setMessage(chatMessage.getMessage())
              .setAttachment(chatMessage.getAttachment())
              .setSenderId(chatMessage.getSenderId())
              .createChatMessage();

          // Add message
          update.put("messages/" + chatId + "/" + message.getMessageId(), message);

          Stream.of(snapshots)
              .map(DataSnapshot::getKey)
              .forEach(userId -> {
                // Update last message
                update.put("users/" + userId + "/chats/" + chatId + "/lastMessage",
                    getLastMessage(message.getMessage()));
                // Update last message ts
                update.put("users/" + userId + "/chats/" + chatId + "/lastMessageTs",
                    ServerValue.TIMESTAMP);
                // Update last sender id
                update.put("users/" + userId + "/chats/" + chatId + "/lastSenderId",
                    chatMessage.getSenderId());
              });

          rootRef.updateChildren(update);
        });
  }

  public Observable<FirebaseChildEvent> observeChats(@Nonnull String userId) {
    return rxFirebaseDb.observeChildEvent(usersRef.child(userId).child("chats"));
  }

  public Observable<FirebaseChildEvent> getChangedChats(@Nonnull String userId) {
    return rxFirebaseDb.observeChildChanged(usersRef.child(userId).child("chats"));
  }

  public Observable<FirebaseChildEvent> getMessages(@Nonnull String chatId) {
    return rxFirebaseDb.observeChildAdded(messagesRef.child(chatId));
  }

  public Observable<ChatUser> getChatUser(@Nonnull String userId) {
    return rxFirebaseDb.observeSingleValue(usersRef.child(userId))
        .map(snapshot -> snapshot.getValue(ChatUser.class));
  }

  public void setLastSeen(@Nonnull String userId) {
    usersRef.child(userId).child("lastSeen").setValue(ServerValue.TIMESTAMP);
  }

  public Observable<DataSnapshot> getLastSeen(@Nonnull String userId) {
    return rxFirebaseDb.observeSingleValue(usersRef.child(userId).child("lastSeen"));
  }
}
