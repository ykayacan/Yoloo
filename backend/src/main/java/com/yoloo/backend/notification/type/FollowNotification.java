package com.yoloo.backend.notification.type;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

@RequiredArgsConstructor(staticName = "create")
public class FollowNotification implements NotificationBundle {

  @NonNull private Account sender;

  @NonNull private Key<Account> receiverKey;

  @NonNull private DeviceRecord record;

  @Override public Notification getNotification() {
    return Notification.builder()
        .senderKey(sender.getKey())
        .receiverKey(receiverKey)
        .senderUsername(sender.getUsername())
        .senderAvatarUrl(sender.getAvatarUrl())
        .action(Action.FOLLOW)
        .created(DateTime.now())
        .build();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value("action", Action.FOLLOW.getValueString())
        .value("sender", sender.getWebsafeId())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
