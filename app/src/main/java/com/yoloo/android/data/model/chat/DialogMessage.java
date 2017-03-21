package com.yoloo.android.data.model.chat;

import com.yoloo.android.chatkit.commons.models.IMessage;
import com.yoloo.android.chatkit.commons.models.IUser;
import com.yoloo.android.data.model.firebase.ChatMessage;

import java.util.Date;

public class DialogMessage implements IMessage {
  private String id;
  private String text;
  private IUser user;
  private Date created;

  public DialogMessage(ChatMessage message) {
    this(message.getId(), message.getMessage(), NormalUser.of(message.getUser()));
  }

  public DialogMessage(String id, String text, IUser user) {
    this.id = id;
    this.text = text;
    this.user = user;
    this.created = new Date();
  }

  @Override public String getId() {
    return id;
  }

  @Override public String getText() {
    return text;
  }

  @Override public IUser getUser() {
    return user;
  }

  @Override public Date getCreatedAt() {
    return created;
  }
}
