package com.yoloo.android.data.chat;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;
import com.yoloo.android.data.chat.firebase.ChatMessage;
import java.util.Date;

public class DefaultMessage implements IMessage, MessageContentType.Image {

  private final ChatMessage chatMessage;

  public DefaultMessage(ChatMessage chatMessage) {
    this.chatMessage = chatMessage;
  }

  @Override public String getId() {
    return chatMessage.getMessageId();
  }

  @Override public String getText() {
    return chatMessage.getMessage();
  }

  @Override public IUser getUser() {
    return new DefaultMessageUser(chatMessage.getSenderId());
  }

  @Override public Date getCreatedAt() {
    return chatMessage.getTime() == null ? new Date() : new Date((long) chatMessage.getTime());
  }

  @Override public String getImageUrl() {
    return chatMessage.getAttachment();
  }

  private static final class DefaultMessageUser implements IUser {

    private final String userId;

    private DefaultMessageUser(String userId) {
      this.userId = userId;
    }

    @Override public String getId() {
      return userId;
    }

    @Override public String getName() {
      return "";
    }

    @Override public String getAvatar() {
      return "";
    }
  }
}
