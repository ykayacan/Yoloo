package com.yoloo.android.data.chat.firebase;

import java.util.Map;

public class ChatUser {
  public static final int ROLE_ADMIN = 100;
  public static final int ROLE_MODERATOR = 200;
  public static final int ROLE_MEMBER = 300;

  private String userId;
  private String username;
  private String avatar;
  private Object lastSeen;
  private Map<String, Chat> chats;

  ChatUser() {
  }

  ChatUser(String userId, String username, String avatar, Map<String, Chat> chats, Object lastSeen) {
    this.userId = userId;
    this.username = username;
    this.avatar = avatar;
    this.lastSeen = lastSeen;
    this.chats = chats;
  }

  public String getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public Object getLastSeen() {
    return lastSeen;
  }

  public String getAvatar() {
    return avatar;
  }

  public Map<String, Chat> getChats() {
    return chats;
  }

  @Override public String toString() {
    return "ChatUser{" +
        "userId='" + userId + '\'' +
        ", username='" + username + '\'' +
        ", avatar='" + avatar + '\'' +
        ", lastSeen=" + lastSeen +
        ", chats=" + chats +
        '}';
  }
}
