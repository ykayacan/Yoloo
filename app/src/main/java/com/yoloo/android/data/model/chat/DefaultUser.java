package com.yoloo.android.data.model.chat;

import com.yoloo.android.chatkit.commons.models.IUser;

public class DefaultUser implements IUser {
  private String id;
  private String name;
  private String avatar;
  private boolean online;

  public DefaultUser(String id, String name, String avatar, boolean online) {
    this.id = id;
    this.name = name;
    this.avatar = avatar;
    this.online = online;
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

  public boolean isOnline() {
    return online;
  }
}
