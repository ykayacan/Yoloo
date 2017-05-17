package com.yoloo.android.data.chat;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import java.util.Date;

public class LastMessage implements IMessage {

  private final String lastMessage;
  private final long lastMessageTs;

  public LastMessage(String lastMessage, long lastMessageTs) {
    this.lastMessage = lastMessage;
    this.lastMessageTs = lastMessageTs;
  }

  @Override public String getId() {
    return null;
  }

  @Override public String getText() {
    return lastMessage;
  }

  @Override public IUser getUser() {
    return new LastMessageUser();
  }

  @Override public Date getCreatedAt() {
    return new Date(lastMessageTs);
  }

  private static final class LastMessageUser implements IUser {

    @Override public String getId() {
      return "";
    }

    @Override public String getName() {
      return "";
    }

    @Override public String getAvatar() {
      return "";
    }
  }
}
