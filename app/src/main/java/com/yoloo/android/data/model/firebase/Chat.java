package com.yoloo.android.data.model.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

public final class Chat {

  private String id;
  private String title;
  private String coverImageUrl;
  private String lastMessage;
  private Integer missed;
  private Object created;
  private Object updated;

  public Chat() {
    this.created = ServerValue.TIMESTAMP;
    this.updated = ServerValue.TIMESTAMP;
    this.missed = 0;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public Chat setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
  }

  public Chat setCoverImageUrl(String coverImageUrl) {
    this.coverImageUrl = coverImageUrl;
    return this;
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public Chat setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
    return this;
  }

  public Integer getMissed() {
    return missed;
  }

  public Chat increaseMissedMessages() {
    ++this.missed;
    return this;
  }

  public Object getCreated() {
    return created;
  }

  public Object getUpdated() {
    return updated;
  }

  public Chat updateTimestamp() {
    this.updated = ServerValue.TIMESTAMP;
    return this;
  }

  @Exclude public long getTimestampCreated() {
    return (long) created;
  }

  @Exclude public long getTimestampUpdated() {
    return (long) updated;
  }

  @Override public String toString() {
    return "Chat{" +
        "title='" + title + '\'' +
        ", coverImageUrl='" + coverImageUrl + '\'' +
        ", lastMessage='" + lastMessage + '\'' +
        ", created=" + created +
        ", updated=" + updated +
        '}';
  }
}
