package com.yoloo.backend.notification.type;

import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.post.PostEntity;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class AcceptNotifiable implements Notifiable {

  private Account sender;
  private DeviceRecord record;
  private PostEntity postEntity;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification
        .builder()
        .senderKey(sender.getKey())
        .receiverKey(record.getParent())
        .action(Action.ACCEPT)
        .payload(PushConstants.POST_ID, postEntity.getWebsafeId())
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody
        .builder()
        .value(PushConstants.ACTION, Action.ACCEPT.getValueString())
        .value(PushConstants.SENDER_USERNAME, sender.getWebsafeId())
        .value(PushConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .value(PushConstants.POST_ID, postEntity.getWebsafeId())
        .build();

    return PushMessage.builder().to(record.getRegId()).data(dataBody).build();
  }
}
