package com.yoloo.backend.notification.type;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import ix.Ix;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class FollowerPostNotification implements Notifiable {

  private final String postId;
  private final String username;
  private final Collection<DeviceRecord> records;

  @Override public List<Notification> getNotifications() {
    return Collections.emptyList();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody
        .builder()
        .value(PushConstants.ACTION, Action.FOLLOWER_POST.getValueString())
        .value(PushConstants.POST_ID, postId)
        .value(PushConstants.SENDER_USERNAME, username)
        .build();

    return PushMessage.builder()
        .registrationIds(Ix.from(records).map(DeviceRecord::getRegId).toList())
        .data(dataBody)
        .build();
  }
}
