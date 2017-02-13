package com.yoloo.backend.notification.type;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class GameBonusNotification implements NotificationBundle {

  private DeviceRecord record;

  private int points;
  private int bounties;

  @Override public List<Notification> getNotifications() {
    Notification notification = Notification.builder()
        .receiverKey(record.getParentUserKey())
        .action(Action.GAME)
        .payload("points", points)
        .payload("bounties", bounties)
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(PushConstants.ACTION, Action.GAME.getValueString())
        .value("points", String.valueOf(points))
        .value("badges", String.valueOf(bounties))
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
