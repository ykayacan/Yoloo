package com.yoloo.android.data.model.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

public class ChatMessage {

  @Exclude private String id;
  @Exclude private String dialogId;
  private ChatUser user;
  private String message;
  private Object created;

  public ChatMessage() {
  }

  public ChatMessage(ChatUser user, String dialogId, String message) {
    this.user = user;
    this.dialogId = dialogId;
    this.message = message;
    this.created = ServerValue.TIMESTAMP;
  }

  public ChatUser getUser() {
    return user;
  }

  public String getMessage() {
    return message;
  }

  public Object getCreated() {
    return created;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDialogId() {
    return dialogId;
  }

  public void setDialogId(String dialogId) {
    this.dialogId = dialogId;
  }

  @Exclude public long getTimestampCreated() {
    return (long) created;
  }

  @Override public String toString() {
    return "ChatMessage{" +
        "id='" + id + '\'' +
        ", dialogId='" + dialogId + '\'' +
        ", user=" + user +
        ", message='" + message + '\'' +
        ", created=" + created +
        '}';
  }
}
