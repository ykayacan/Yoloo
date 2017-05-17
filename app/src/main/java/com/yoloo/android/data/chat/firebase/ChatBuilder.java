package com.yoloo.android.data.chat.firebase;

import com.google.firebase.database.ServerValue;

public class ChatBuilder {
  private String chatId;
  private String createdByUserId;
  private String chatName;
  private String chatPhoto;
  private int type;
  private String lastMessage;
  private Object lastMessageTs;
  private String lastSenderId;
  private int unreadCount = 0;

  public ChatBuilder setChatId(String chatId) {
    this.chatId = chatId;
    return this;
  }

  public ChatBuilder setCreatedByUserId(String createdByUserId) {
    this.createdByUserId = createdByUserId;
    return this;
  }

  public ChatBuilder setChatName(String chatName) {
    this.chatName = chatName;
    return this;
  }

  public ChatBuilder setChatPhoto(String chatPhoto) {
    this.chatPhoto = chatPhoto;
    return this;
  }

  public ChatBuilder setType(int type) {
    this.type = type;
    return this;
  }

  public ChatBuilder setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
    return this;
  }

  public ChatBuilder setLastMessageTs(Object lastMessageTs) {
    this.lastMessageTs = lastMessageTs;
    return this;
  }

  public ChatBuilder setLastSenderId(String lastSenderId) {
    this.lastSenderId = lastSenderId;
    return this;
  }

  public ChatBuilder setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
    return this;
  }

  public Chat createChat() {
    return new Chat(chatId, createdByUserId, chatName, chatPhoto, type, lastMessage, lastMessageTs,
        lastSenderId, unreadCount, ServerValue.TIMESTAMP);
  }
}