package com.yoloo.android.data.model;

import com.yoloo.backend.yolooApi.model.NotificationDTO;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NotificationRealm extends RealmObject {

  public static final String FOLLOW = "FOLLOW";
  public static final String COMMENT = "COMMENT";
  public static final String MENTION = "MENTION";
  public static final String GAME = "GAME";
  public static final String ACCEPT = "ACCEPT";

  @PrimaryKey private String id;
  private String senderId;
  private String senderUsername;
  private String senderAvatarUrl;
  private String action;
  private String message;
  private String postId;
  private RealmList<PayloadMap> payload;
  @Index private Date created;

  public NotificationRealm() {
  }

  public NotificationRealm(NotificationDTO dto) {
    id = dto.getId();
    senderId = dto.getSenderId();
    senderUsername = dto.getSenderUsername();
    senderAvatarUrl = dto.getSenderAvatarUrl();
    action = dto.getAction();
    created = new Date(dto.getCreated().getValue());

    payload = new RealmList<>();
    if (dto.getPayload() != null) {
      for (Map.Entry<String, Object> entry : dto.getPayload().entrySet()) {
        payload.add(new PayloadMap(entry.getKey(), (String) entry.getValue()));
      }
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

  public String getPostId() {
    return postId;
  }

  public NotificationRealm setPostId(String postId) {
    this.postId = postId;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public NotificationRealm setCreated(Date created) {
    this.created = created;
    return this;
  }

  public List<PayloadMap> getPayload() {
    return payload;
  }

  public NotificationRealm setPayload(RealmList<PayloadMap> payload) {
    this.payload = payload;
    return this;
  }
}
