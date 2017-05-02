package com.yoloo.android.data.model.chat;

import android.os.Parcel;
import android.os.Parcelable;
import com.stfalcon.chatkit.commons.models.IUser;

public class User implements IUser, Parcelable {

  public static final int ROLE_ADMIN = 0;
  public static final int ROLE_MEMBER = 1;

  public static final Creator<User> CREATOR = new Creator<User>() {
    @Override
    public User createFromParcel(Parcel in) {
      return new User(in);
    }

    @Override
    public User[] newArray(int size) {
      return new User[size];
    }
  };

  private String id;
  private String name;
  private String avatar;
  private int role;
  private int unreadCount = 0;

  private String dialogName;
  private String dialogPhoto;

  public User() {
  }

  public User(String id, String name, String avatar, int role, String dialogName,
      String dialogPhoto) {
    this(id, name, avatar, role, 0, dialogName, dialogPhoto);
  }

  private User(String id, String name, String avatar, int role, int unreadCount, String dialogName,
      String dialogPhoto) {
    this.id = id;
    this.name = name;
    this.avatar = avatar;
    this.role = role;
    this.unreadCount = unreadCount;
    this.dialogName = dialogName;
    this.dialogPhoto = dialogPhoto;
  }

  protected User(Parcel in) {
    id = in.readString();
    name = in.readString();
    avatar = in.readString();
    role = in.readInt();
    unreadCount = in.readInt();
    dialogName = in.readString();
    dialogPhoto = in.readString();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getAvatar() {
    return avatar;
  }

  public int getRole() {
    return role;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public int increaseUnreadCounter() {
    return ++unreadCount;
  }

  public String getDialogName() {
    return dialogName;
  }

  public String getDialogPhoto() {
    return dialogPhoto;
  }

  public User withUnreadCount(int unreadCount) {
    return new User(id, name, avatar, role, unreadCount, dialogName, dialogPhoto);
  }

  @Override
  public String toString() {
    return "User{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", avatar='"
        + avatar
        + '\''
        + ", role="
        + role
        + ", unreadCount="
        + unreadCount
        + ", dialogName='"
        + dialogName
        + '\''
        + ", dialogPhoto='"
        + dialogPhoto
        + '\''
        + '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(avatar);
    dest.writeInt(role);
    dest.writeInt(unreadCount);
    dest.writeString(dialogName);
    dest.writeString(dialogPhoto);
  }
}
