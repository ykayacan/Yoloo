package com.yoloo.backend.notification.dto;

import com.yoloo.backend.notification.Action;
import java.util.Date;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationDTO {
  private String id;
  private String senderId;
  private String senderUsername;
  private String senderAvatarUrl;
  private Action action;
  private Map<String, Object> payload;
  private Date created;
}
