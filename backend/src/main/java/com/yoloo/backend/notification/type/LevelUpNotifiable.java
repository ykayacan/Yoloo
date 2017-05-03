package com.yoloo.backend.notification.type;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.game.Tracker;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class LevelUpNotifiable implements Notifiable {

  private DeviceRecord record;
  private Tracker tracker;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification
        .builder()
        .receiverKey(record.getParent())
        .action(Action.GAME)
        .payload(PushConstants.LEVEL, tracker.getLevel())
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody
        .builder()
        .value(PushConstants.ACTION, Action.GAME.getValueString())
        .value(PushConstants.GAME_ACTION, Action.GameAction.LEVEL_UP.getValueString())
        .value(PushConstants.LEVEL, String.valueOf(tracker.getLevel()))
        .build();

    return PushMessage.builder().to(record.getRegId()).data(dataBody).build();
  }
}
