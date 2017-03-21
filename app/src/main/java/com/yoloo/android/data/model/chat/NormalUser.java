package com.yoloo.android.data.model.chat;

import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.data.model.firebase.ChatUser;

public class NormalUser implements IUser {
  private String id;
  private String name;
  private String avatar;

  public static NormalUser of(ChatUser user) {
    return new NormalUser(user.getId(), user.getName(), user.getAvatarUrl());
  }

  public NormalUser(String id, String name, String avatar) {
    this.id = id;
    this.name = name;
    this.avatar = avatar;
  }

  @Override public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  @Override public String toString() {
    return "NormalUser{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", avatar='" + avatar + '\'' +
        '}';
  }
}
