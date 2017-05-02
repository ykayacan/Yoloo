package com.yoloo.android.data.model.chat;

import android.os.Parcel;
import android.os.Parcelable;
import com.annimon.stream.Stream;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.stfalcon.chatkit.commons.models.IDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialog implements IDialog<Message>, Parcelable {

  public static final Creator<Dialog> CREATOR = new Creator<Dialog>() {
    @Override
    public Dialog createFromParcel(Parcel in) {
      return new Dialog(in);
    }

    @Override
    public Dialog[] newArray(int size) {
      return new Dialog[size];
    }
  };

  private String id;
  private String dialogPhoto;
  private String dialogName;
  private HashMap<String, User> members = new HashMap<>();
  private Message lastMessage;
  private String lastSenderId;
  private String lastMessageString;
  private Object createdTs;
  private Object updatedTs;
  private CurrentUserLoader currentUserLoader;

  public Dialog() {
  }

  public Dialog(String dialogPhoto, String dialogName, HashMap<String, User> members,
      String lastSenderId, CurrentUserLoader listener) {
    this(null, dialogPhoto, dialogName, members, null, lastSenderId, null, null, listener);
  }

  private Dialog(String id, String dialogPhoto, String dialogName, HashMap<String, User> members,
      String lastMessage, String lastSenderId, Object createdTs, Object updatedTs,
      CurrentUserLoader listener) {
    this.id = id;
    this.dialogPhoto = dialogPhoto;
    this.dialogName = dialogName;
    this.members = members;
    this.lastSenderId = lastSenderId;

    this.lastMessage = new Message(lastMessage, Stream
        .of(getMembers().values())
        .filter(value -> value.getId().equals(lastSenderId))
        .findSingle()
        .orElse(null));
    this.currentUserLoader = listener;
    this.createdTs = createdTs == null ? ServerValue.TIMESTAMP : createdTs;
    this.updatedTs = updatedTs == null ? ServerValue.TIMESTAMP : updatedTs;
  }

  protected Dialog(Parcel in) {
    id = in.readString();
    dialogPhoto = in.readString();
    dialogName = in.readString();
    lastMessage = in.readParcelable(Message.class.getClassLoader());
    lastSenderId = in.readString();
    lastMessageString = in.readString();
    members = (HashMap<String, User>) in.readSerializable();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(dialogPhoto);
    dest.writeString(dialogName);
    dest.writeParcelable(lastMessage, flags);
    dest.writeString(lastSenderId);
    dest.writeString(lastMessageString);
    dest.writeSerializable(members);
  }

  @Override
  public String getId() {
    return id;
  }

  public Dialog withId(String id) {
    return new Dialog(id, dialogPhoto, dialogName, members, lastMessageString, lastSenderId,
        createdTs, updatedTs, currentUserLoader);
  }

  @Exclude
  @Override
  public String getDialogPhoto() {
    dialogPhoto = Stream
        .of(members.values())
        .filter(value -> value.getId().equals(currentUserLoader.getUserId()))
        .single()
        .getDialogPhoto();
    return dialogPhoto;
  }

  @Exclude
  @Override
  public String getDialogName() {
    dialogName = Stream
        .of(members.values())
        .filter(value -> value.getId().equals(currentUserLoader.getUserId()))
        .single()
        .getDialogName();
    return dialogName;
  }

  @Exclude
  @Override
  public List<User> getUsers() {
    return new ArrayList<>(members.values());
  }

  public Map<String, User> getMembers() {
    return members;
  }

  @Exclude
  @Override
  public Message getLastMessage() {
    return lastMessage;
  }

  @Exclude
  @Override
  public void setLastMessage(Message message) {
    this.lastMessage = message;
    this.lastMessageString = this.lastMessage.getText();
  }

  @Exclude
  @Override
  public int getUnreadCount() {
    return Stream
        .of(members.values())
        .filter(value -> !value.getId().equals(currentUserLoader.getUserId()))
        .single()
        .getUnreadCount();
  }

  public Dialog withCurrentUser(String userId) {
    return new Dialog(id, dialogPhoto, dialogName, members, lastMessageString, lastSenderId,
        createdTs, updatedTs, () -> userId);
  }

  public Object getCreatedTs() {
    return createdTs;
  }

  public Object getUpdatedTs() {
    return updatedTs;
  }

  public String getLastMessageString() {
    return lastMessageString;
  }

  public String getLastSenderId() {
    return lastSenderId;
  }

  @Exclude
  public long getTimestampCreated() {
    return (long) updatedTs;
  }

  @Exclude
  public long getTimestampUpdated() {
    return (long) updatedTs;
  }

  @Override
  public String toString() {
    return "Dialog{"
        + "id='"
        + id
        + '\''
        + ", dialogPhoto='"
        + dialogPhoto
        + '\''
        + ", dialogName='"
        + dialogName
        + '\''
        + ", members="
        + members
        + ", lastMessage="
        + lastMessage
        + ", lastSenderId='"
        + lastSenderId
        + '\''
        + ", lastMessageString='"
        + lastMessageString
        + '\''
        + ", createdTs="
        + createdTs
        + ", updatedTs="
        + updatedTs
        + ", currentUserLoader="
        + currentUserLoader
        + '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public interface CurrentUserLoader {
    String getUserId();
  }
}
