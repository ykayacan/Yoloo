package com.yoloo.backend.notification.type;

import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.MessageConstants;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class FollowNotification implements NotificationBundle {

  private Account sender;
  private DeviceRecord record;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification.builder()
        .senderKey(sender.getKey())
        .receiverKey(record.getParentUserKey())
        .senderId(sender.getWebsafeId())
        .senderUsername(sender.getUsername())
        .senderAvatarUrl(sender.getAvatarUrl())
        .action(Action.FOLLOW)
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(MessageConstants.ACTION, Action.FOLLOW.getValueString())
        .value(MessageConstants.SENDER_USERNAME, sender.getWebsafeId())
        .value(MessageConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
