package com.yoloo.android.data.chat.firebase;

import com.google.firebase.database.Exclude;
import java.util.Date;

public class Chat {
  public static final int CHATBOT = 0;
  public static final int USER = 1;
  public static final int GROUP = 2;

  private String chatId;
  private String createdByUserId;
  private String chatName;
  private String chatPhoto;
  // Chat type
  // 0 - Chatbot
  // 1 - User
  // 2 - Group
  private int type;
  private String lastMessage;
  private Object lastMessageTs;
  private String lastSenderId;
  private int unreadCount;
  private Object createdAt;

  Chat() {
  }

  Chat(String chatId, String createdByUserId, String chatName, String chatPhoto, int type,
      String lastMessage, Object lastMessageTs, String lastSenderId, int unreadCount,
      Object createdAt) {
    this.chatId = chatId;
    this.createdByUserId = createdByUserId;
    this.chatName = chatName;
    this.chatPhoto = chatPhoto;
    this.type = type;
    this.lastMessage = lastMessage;
    this.lastMessageTs = lastMessageTs;
    this.lastSenderId = lastSenderId;
    this.unreadCount = unreadCount;
    this.createdAt = createdAt;
  }

  public String getChatId() {
    return chatId;
  }

  public String getCreatedByUserId() {
    return createdByUserId;
  }

  public String getChatName() {
    return chatName;
  }

  public String getChatPhoto() {
    return chatPhoto;
  }

  public int getType() {
    return type;
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public Object getLastMessageTs() {
    return lastMessageTs;
  }

  public String getLastSenderId() {
    return lastSenderId;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public Object getCreatedAt() {
    return createdAt;
  }

  @Exclude
  public Date getCreatedAtDate() {
    return new Date((Long) createdAt);
  }

  @Override public String toString() {
    return "Chat{" +
        "chatId='" + chatId + '\'' +
        ", createdByUserId='" + createdByUserId + '\'' +
        ", chatName='" + chatName + '\'' +
        ", chatPhoto='" + chatPhoto + '\'' +
        ", type=" + type +
        ", lastMessage='" + lastMessage + '\'' +
        ", lastMessageTs=" + lastMessageTs +
        ", unreadCount=" + unreadCount +
        ", createdAt=" + createdAt +
        '}';
  }
}
