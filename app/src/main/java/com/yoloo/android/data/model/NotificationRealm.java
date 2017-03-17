package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.JsonMap;
import com.yoloo.backend.yolooApi.model.NotificationDTO;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Date;

public class NotificationRealm extends RealmObject {

  public static final String FOLLOW = "1";
  public static final String COMMENT = "2";
  public static final String MENTION = "3";
  public static final String GAME = "4";
  public static final String ACCEPT = "5";

  @PrimaryKey
  private String id;
  private String senderId;
  private String senderUsername;
  private String senderAvatarUrl;
  private String action;
  private String message;
  private String questionId;
  @Index
  private Date created;

  public NotificationRealm() {
  }

  public NotificationRealm(NotificationDTO dto) {
    id = dto.getId();
    senderId = dto.getSenderId();
    senderUsername = dto.getSenderUsername();
    senderAvatarUrl = dto.getSenderAvatarUrl();
    action = dto.getAction();
    created = new Date(dto.getCreated().getValue());

    final JsonMap map = dto.getPayload();
    if (map.containsKey("questionId")) {
      questionId = (String) map.get("questionId");
    }

    if (map.containsKey("message")) {
      message = (String) map.get("message");
    }
  }

  public String getId() {
    return id;
  }

  public NotificationRealm setId(String id) {
    this.id = id;
    return this;
  }

  public String getSenderId() {
    return senderId;
  }

  public NotificationRealm setSenderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  public String getSenderUsername() {
    return senderUsername;
  }

  public NotificationRealm setSenderUsername(String senderUsername) {
    this.senderUsername = senderUsername;
    return this;
  }

  public String getSenderAvatarUrl() {
    return senderAvatarUrl;
  }

  public NotificationRealm setSenderAvatarUrl(String senderAvatarUrl) {
    this.senderAvatarUrl = senderAvatarUrl;
    return this;
  }

  public String getAction() {
    return action;
  }

  public NotificationRealm setAction(String action) {
    this.action = action;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public NotificationRealm setMessage(String message) {
    this.message = message;
    return this;
  }

  public String getQuestionId() {
    return questionId;
  }

  public NotificationRealm setQuestionId(String questionId) {
    this.questionId = questionId;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public NotificationRealm setCreated(Date created) {
    this.created = created;
    return this;
  }
}
