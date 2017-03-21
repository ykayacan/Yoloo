package com.yoloo.android.data.model.firebase;

import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.data.model.AccountRealm;

public final class ChatUser {

  public static final int ROLE_USER = 1;
  public static final int ROLE_MODERATOR = 2;
  public static final int ROLE_ADMIN = 3;

  private String id;
  private String avatarUrl;
  private String name;
  private int role;

  public ChatUser() {
  }

  public ChatUser(IUser user) {
    this.id = user.getId();
    this.avatarUrl = user.getAvatar();
    this.name = user.getName();
  }

  public ChatUser(AccountRealm user, int role) {
    this(user.getId(), user.getAvatarUrl(), user.getUsername(), role);
  }

  public ChatUser(String id, String avatarUrl, String name, int role) {
    this.id = id;
    this.avatarUrl = avatarUrl;
    this.name = name;
    this.role = role;
  }

  public String getId() {
    return id;
  }

  public ChatUser setId(String id) {
    this.id = id;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public ChatUser setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getName() {
    return name;
  }

  public ChatUser setName(String name) {
    this.name = name;
    return this;
  }

  public int getRole() {
    return role;
  }

  public ChatUser setRole(int role) {
    this.role = role;
    return this;
  }

  @Override public String toString() {
    return "ChatUser{" +
        "id='" + id + '\'' +
        ", avatarUrl='" + avatarUrl + '\'' +
        ", name='" + name + '\'' +
        ", role=" + role +
        '}';
  }
}
