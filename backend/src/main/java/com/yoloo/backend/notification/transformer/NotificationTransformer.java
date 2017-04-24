package com.yoloo.backend.notification.transformer;

import com.google.api.server.spi.config.Transformer;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.dto.NotificationDTO;

public class NotificationTransformer implements Transformer<Notification, NotificationDTO> {
  @Override
  public NotificationDTO transformTo(Notification in) {
    return NotificationDTO
        .builder()
        .id(in.getWebsafeId())
        .senderId(in.getSenderId())
        .senderUsername(in.getSenderUsername())
        .senderAvatarUrl(
            in.getSenderAvatarUrl() == null ? null : in.getSenderAvatarUrl().getValue())
        .action(in.getAction())
        .payload(in.getPayloads())
        .created(in.getCreated().toDate())
        .build();
  }

  @Override
  public Notification transformFrom(NotificationDTO in) {
    return null;
  }
}
