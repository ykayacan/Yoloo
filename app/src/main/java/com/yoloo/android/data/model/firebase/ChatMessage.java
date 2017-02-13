package com.yoloo.android.data.model.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

public class ChatMessage {

  @Exclude private String chatId;
  private String username;
  private String message;
  private Object created;

  public ChatMessage(String username, String message) {
    this.username = username;
    this.message = message;
    this.created = ServerValue.TIMESTAMP;
  }

  public String getUsername() {
    return username;
  }

  public String getMessage() {
    return message;
  }

  public Object getCreated() {
    return created;
  }

  public String getChatId() {
    return chatId;
  }

  public void setChatId(String chatId) {
    this.chatId = chatId;
  }

  @Exclude public long getTimestampCreated() {
    return (long) created;
  }
}
