package com.yoloo.android.data.model.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Stream;
import com.yoloo.android.chatkit.commons.models.IDialog;
import com.yoloo.android.chatkit.commons.models.IMessage;
import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.data.model.firebase.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NormalDialog implements IDialog, Parcelable {

  public static final Creator<NormalDialog> CREATOR = new Creator<NormalDialog>() {
    @Override
    public NormalDialog createFromParcel(Parcel in) {
      return new NormalDialog(in);
    }

    @Override
    public NormalDialog[] newArray(int size) {
      return new NormalDialog[size];
    }
  };

  private String id;
  private String dialogPhoto;
  private String dialogName;
  private List<? extends IUser> users;
  private IMessage lastMessage;
  private int unreadCount;

  public NormalDialog(String id, String name, String photo, List<? extends IUser> users,
      IMessage lastMessage, int unreadCount) {
    this.id = id;
    this.dialogName = name;
    this.dialogPhoto = photo;
    this.users = users;
    this.lastMessage = lastMessage;
    this.unreadCount = unreadCount;
  }

  protected NormalDialog(Parcel in) {
    id = in.readString();
    dialogPhoto = in.readString();
    dialogName = in.readString();
    unreadCount = in.readInt();
  }

  public static NormalDialog from(Chat chat) {
    return new NormalDialog(
        chat.getId(),
        chat.getName(),
        chat.getChatPhotoUrl(),
        Stream.of(chat.getMembers()).map(Map.Entry::getValue).map(NormalUser::of).toList(),
        NormalMessage.of(chat),
        chat.getUnreadCount());
  }

  @Override public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override public String getDialogPhoto() {
    return dialogPhoto;
  }

  @Override public String getDialogName() {
    return dialogName;
  }

  @Override public List<? extends IUser> getUsers() {
    return users;
  }

  public void setUsers(ArrayList<IUser> users) {
    this.users = users;
  }

  @Override public IMessage getLastMessage() {
    return lastMessage;
  }

  @Override public void setLastMessage(IMessage lastMessage) {
    this.lastMessage = lastMessage;
  }

  @Override public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(dialogPhoto);
    dest.writeString(dialogName);
    dest.writeInt(unreadCount);
  }
}
