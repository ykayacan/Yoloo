package com.yoloo.android.data.chat.firebase;

import com.google.firebase.database.ServerValue;
import java.util.Map;

public class ChatUserBuilder {
  private String userId;
  private String username;
  private String avatar;
  private Map<String, Chat> chats;

  public ChatUserBuilder setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public ChatUserBuilder setUsername(String username) {
    this.username = username;
    return this;
  }

  public ChatUserBuilder setAvatar(String avatar) {
    this.avatar = avatar;
    return this;
  }

  public ChatUserBuilder setChats(Map<String, Chat> chats) {
    this.chats = chats;
    return this;
  }

  public ChatUser createChatUser() {
    return new ChatUser(userId, username, avatar, chats, ServerValue.TIMESTAMP);
  }
}