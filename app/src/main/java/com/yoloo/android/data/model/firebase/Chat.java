package com.yoloo.android.data.model.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.yoloo.android.chatkit.commons.models.IDialog;
import com.yoloo.android.chatkit.commons.models.IUser;

import java.util.HashMap;
import java.util.Map;

public final class Chat {

  private String id;
  private String name;
  private String chatPhotoUrl;
  private String lastMessage;
  private Integer unreadCount;
  private String lastSenderId;
  private Object created;
  private Object updated;
  private Map<String, ChatUser> members = new HashMap<>();

  public Chat(IDialog dialog) {
    this.id = dialog.getId();
    for (IUser user : dialog.getUsers()) {
      members.put(user.getId(), null);
    }
  }

  public Chat() {
    this.created = ServerValue.TIMESTAMP;
    this.updated = ServerValue.TIMESTAMP;
    this.unreadCount = 0;
  }

  public String getId() {
    return id;
  }

  public Chat setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Chat setName(String name) {
    this.name = name;
    return this;
  }

  public String getChatPhotoUrl() {
    return chatPhotoUrl;
  }

  public Chat setChatPhotoUrl(String chatPhotoUrl) {
    this.chatPhotoUrl = chatPhotoUrl;
    return this;
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public Chat setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
    return this;
  }

  public Integer getUnreadCount() {
    return unreadCount;
  }

  public Map<String, ChatUser> getMembers() {
    return members;
  }

  public Chat setMembers(Map<String, ChatUser> members) {
    this.members = members;
    return this;
  }

  public Chat increaseUnreadCount() {
    ++this.unreadCount;
    return this;
  }

  public Object getCreated() {
    return created;
  }

  public Object getUpdated() {
    return updated;
  }

  public String getLastSenderId() {
    return lastSenderId;
  }

  public Chat setLastSenderId(String lastSenderId) {
    this.lastSenderId = lastSenderId;
    return this;
  }

  public Chat updateTimestamp() {
    this.updated = ServerValue.TIMESTAMP;
    return this;
  }

  @Exclude public long getTimestampCreated() {
    return (long) created;
  }

  @Exclude public long getTimestampUpdated() {
    return (long) updated;
  }

  @Override public String toString() {
    return "Chat{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", chatPhotoUrl='" + chatPhotoUrl + '\'' +
        ", lastMessage='" + lastMessage + '\'' +
        ", unreadCount=" + unreadCount +
        ", lastSenderId='" + lastSenderId + '\'' +
        ", created=" + created +
        ", updated=" + updated +
        ", members=" + members +
        '}';
  }
}
