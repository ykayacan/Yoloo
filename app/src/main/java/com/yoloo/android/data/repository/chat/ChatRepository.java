package com.yoloo.android.data.repository.chat;

import android.support.annotation.NonNull;
import com.annimon.stream.Stream;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.yoloo.android.data.chat.Dialog;
import com.yoloo.android.data.chat.Message;
import com.yoloo.android.data.chat.User;
import com.yoloo.android.rxfirebase.FirebaseChildEvent;
import com.yoloo.android.rxfirebase.RxFirebaseDatabase;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;

public class ChatRepository {

  private static ChatRepository instance;

  private DatabaseReference rootRef;
  private DatabaseReference usersRef;
  private DatabaseReference chatsRef;
  private DatabaseReference messagesRef;
  private DatabaseReference membersRef;

  private RxFirebaseDatabase rxFirebaseDb;

  private ChatRepository() {
    rootRef = FirebaseDatabase.getInstance().getReference();
    usersRef = rootRef.child("users");
    chatsRef = rootRef.child("chats");
    messagesRef = rootRef.child("messages");
    membersRef = rootRef.child("members");
    rxFirebaseDb = RxFirebaseDatabase.getInstance();
  }

  public static ChatRepository getInstance() {
    if (instance == null) {
      instance = new ChatRepository();
    }
    return instance;
  }

  public Observable<Dialog> createDialog(@NonNull Dialog dialog) {
    final String targetUserId = dialog.getUsers().get(1).getId();

    return rxFirebaseDb
        .observeSingleValue(membersRef.child(dialog.getUsers().get(0).getId()))
        .flatMapSingle(s -> {
          if (s.exists()) {
            Timber.d("Getting existing chat");
            return Observable
                .fromIterable(s.getChildren())
                .filter(snapshot -> targetUserId.equals(snapshot.getKey()))
                .map(snapshot -> snapshot.getValue(String.class))
                .flatMap(dialogId -> rxFirebaseDb.observeSingleValue(chatsRef.child(dialogId)))
                .doOnNext(snapshot -> Timber.d("Snapshot: %s", snapshot))
                .map(snapshot -> snapshot.getValue(Dialog.class))
                .firstElement()
                .toSingle();
          } else {
            Timber.d("Creating new chat");
            return Single.fromCallable(() -> {
              Map<String, Object> update = new HashMap<>();

              final String dialogId = rootRef.push().getKey();

              Dialog newDialog = dialog.withId(dialogId);
              // chats/$chatId/meta
              update.put("chats/" + dialogId + "/meta", newDialog);

              // Only create dialog for admin.
              // If admin writes something to target user then update the target's chat list.
              // users/$userId/$chatId
              for (User user : dialog.getUsers()) {
                if (user.getRole() == User.ROLE_ADMIN) {
                  update.put("users/" + user.getId() + "/chats/" + dialogId, user.getRole());
                }
              }

              rootRef.updateChildren(update);

              return newDialog;
            });
          }
        });

    /*return rxFirebaseDb
        .observeSingleValue(membersRef.child(dialog.getUsers().get(0).getId()))
        .filter(DataSnapshot::exists)
        .flatMapIterable(DataSnapshot::getChildren)
        .map(snapshot -> snapshot.getValue(String.class).split(":"))
        .filter(pair -> targetUserId.equals(pair[0]))
        .flatMap(pair -> rxFirebaseDb.observeSingleValue(chatsRef.child(pair[1])))
        .map(snapshot -> snapshot.getValue(Dialog.class))
        .firstElement()
        .map(persistentDialog -> {
          if (persistentDialog == null) {
            Map<String, Object> update = new HashMap<>();

            final String dialogId = rootRef.push().getKey();

            Dialog newDialog = dialog.withId(dialogId);
            // chats/$chatId/meta
            update.put("chats/" + dialogId + "/meta", dialog);

            // Only create dialog for admin.
            // If admin writes something to target user then update the target's chat list.
            // users/$userId/$chatId
            for (User user : dialog.getUsers()) {
              if (user.getRole() == User.ROLE_ADMIN) {
                update.put("users/" + user.getId() + "/chats/" + dialogId, user.getRole());
              }
            }

            rootRef.updateChildren(update);

            return newDialog;
          }

          return persistentDialog;
        })
        .toSingle();*/

    /*membersRef
        .child(dialog.getUsers().get(0).getId())
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
              String[] memberPair = snapshot.getValue(String.class).split(":");
              if (targetUserId.equals(memberPair[0])) {
                chatsRef
                    .child(memberPair[1])
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                        newDialog = dataSnapshot.getValue(Dialog.class);
                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                    });

                break;
              } else {

              }
            }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
        });*/
  }

  public void addDialogToTargetUser(@NonNull Dialog dialog) {
    Map<String, Object> update = new HashMap<>();

    final String targetUserId = Stream
        .of(dialog.getUsers())
        .filter(value -> value.getRole() == User.ROLE_MEMBER)
        .map(User::getId)
        .single();

    update.put("users/" + targetUserId + "/chats/" + dialog.getId(), User.ROLE_MEMBER);
    update.put("chats/" + dialog.getId() + "/meta/members/" + targetUserId + "/unreadCount/", 1);

    final String user1Id = dialog.getUsers().get(0).getId();
    final String user2Id = dialog.getUsers().get(1).getId();

    update.put("members/" + user1Id + "/" + user2Id, dialog.getId());
    update.put("members/" + user2Id + "/" + user1Id, dialog.getId());

    rootRef.updateChildren(update);
  }

  public void deleteDialog(@NonNull Dialog dialog) {
    Map<String, Object> delete = new HashMap<>();
    delete.put("chats/" + dialog.getId(), null);
    delete.put("messages/" + dialog.getId(), null);

    Stream.of(dialog.getUsers()).forEach(user -> {
      delete.put("users/" + user.getId() + "/chats/" + dialog.getId(), null);
      delete.put("members/" + user.getId(), null);
    });

    rootRef.updateChildren(delete);
  }

  public Observable<FirebaseChildEvent> getMessagesByDialogId(@NonNull String senderId,
      @NonNull String dialogId) {
    Timber.d("getMessagesByDialogId(): %s", senderId);
    chatsRef
        .child(dialogId)
        .child("meta")
        .child("members")
        .child(senderId)
        .child("unreadCount")
        .setValue(0);
    return rxFirebaseDb.observeChildEvent(messagesRef.child(dialogId));
  }

  public Message sendMessage(@NonNull Dialog dialog, @NonNull String targetUserId,
      @NonNull Message message) {
    final String messageId = rootRef.push().getKey();
    message = message.withId(messageId);

    Map<String, Object> update = new HashMap<>();
    update.put("messages/" + dialog.getId() + "/" + messageId, message);
    update.put("chats/" + dialog.getId() + "/meta/lastSenderId", message.getUser().getId());
    update.put("chats/" + dialog.getId() + "/meta/lastMessageString",
        getTrimmedText(message.getText()));
    update.put("chats/" + dialog.getId() + "/meta/updatedTs", ServerValue.TIMESTAMP);

    final User targetUser =
        Stream.of(dialog.getUsers()).filter(value -> value.getId().equals(targetUserId)).single();

    update.put("chats/" + dialog.getId() + "/meta/members/" + targetUserId + "/unreadCount",
        targetUser.increaseUnreadCounter());

    rootRef.updateChildren(update);

    return message;
  }

  public Observable<FirebaseChildEvent> getDialogsByUserId(@NonNull String userId) {
    return rxFirebaseDb
        .observeChildEvent(usersRef.child(userId).child("chats"))
        .map(event -> event.getDataSnapshot().getKey())
        .flatMap(chatId -> rxFirebaseDb.observeChildEvent(chatsRef.child(chatId)));
  }

  private String getTrimmedText(String content) {
    return content.length() > 38 ? content.substring(0, 38).concat("...") : content;
  }
}
