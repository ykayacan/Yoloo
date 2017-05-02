package com.yoloo.android.data.model.chat;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;
import java.util.Date;
import java.util.Map;

public class Message implements IMessage, MessageContentType.Image, MessageContentType, Parcelable {

  public static final Creator<Message> CREATOR = new Creator<Message>() {
    @Override
    public Message createFromParcel(Parcel in) {
      return new Message(in);
    }

    @Override
    public Message[] newArray(int size) {
      return new Message[size];
    }
  };

  private String attachmentUrl;
  private String id;
  private String text;
  private User user;
  private Object createdTs;

  public Message() {
  }

  public Message(String text, User user) {
    this(null, text, null, user);
  }

  private Message(String id, String text, String attachmentUrl, User user) {
    this.id = id;
    this.text = text;
    this.attachmentUrl = attachmentUrl;
    this.user = user;
    this.createdTs = ServerValue.TIMESTAMP;
  }

  protected Message(Parcel in) {
    attachmentUrl = in.readString();
    id = in.readString();
    text = in.readString();
    user = in.readParcelable(User.class.getClassLoader());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public IUser getUser() {
    return user;
  }

  @Exclude
  @Override
  public Date getCreatedAt() {
    // check timestamp value to avoid crash
    if (createdTs instanceof Map) {
      return new Date();
    }

    return new Date((long) createdTs);
  }

  @Override
  public String getImageUrl() {
    return attachmentUrl;
  }

  public Message withId(String id) {
    return new Message(id, text, attachmentUrl, user);
  }

  public Message withImage(String attachmentUrl) {
    return new Message(id, text, attachmentUrl, user);
  }

  public Object getCreatedTs() {
    return createdTs;
  }

  @Override
  public String toString() {
    return "Message{"
        + "id='"
        + id
        + '\''
        + ", text='"
        + text
        + '\''
        + ", createdTs="
        + createdTs
        + ", user="
        + user
        + ", image="
        + attachmentUrl
        + '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(attachmentUrl);
    dest.writeString(id);
    dest.writeString(text);
    dest.writeParcelable(user, flags);
  }
}
