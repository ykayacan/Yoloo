package com.yoloo.android.data.chat.firebase;

import com.google.firebase.database.ServerValue;

public class ChatMessage {
  private String messageId;
  private String senderId;
  private String message;
  private String attachment;
  private Object time;

  ChatMessage() {
  }

  ChatMessage(String messageId, String senderId, String message, String attachment) {
    this.messageId = messageId;
    this.senderId = senderId;
    this.message = message;
    this.attachment = attachment;
    this.time = ServerValue.TIMESTAMP;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getSenderId() {
    return senderId;
  }

  public String getMessage() {
    return message;
  }

  public String getAttachment() {
    return attachment;
  }

  public Object getTime() {
    return time;
  }

  @Override public String toString() {
    return "DefaultMessage{" +
        "messageId='" + messageId + '\'' +
        ", senderId='" + senderId + '\'' +
        ", message='" + message + '\'' +
        ", attachment='" + attachment + '\'' +
        ", time=" + time +
        '}';
  }
}
