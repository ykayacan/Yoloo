package com.yoloo.android.data.chat.firebase;

public class ChatMessageBuilder {
  private String messageId;
  private String senderId;
  private String message;
  private String attachment;

  public ChatMessageBuilder setMessageId(String messageId) {
    this.messageId = messageId;
    return this;
  }

  public ChatMessageBuilder setSenderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  public ChatMessageBuilder setMessage(String message) {
    this.message = message;
    return this;
  }

  public ChatMessageBuilder setAttachment(String attachment) {
    this.attachment = attachment;
    return this;
  }

  public ChatMessage createChatMessage() {
    return new ChatMessage(messageId, senderId, message, attachment);
  }
}