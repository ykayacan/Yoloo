package com.yoloo.backend.notification.type;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.gamification.Tracker;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

@RequiredArgsConstructor(staticName = "create")
public class GamePointNotification implements NotificationBundle {

  @NonNull private Key<Account> receiverKey;

  @NonNull private DeviceRecord record;

  @NonNull private Tracker tracker;

  @Override public Notification getNotification() {
    return Notification.builder()
        .receiverKey(receiverKey)
        .action(Action.GAME)
        .object("points", tracker.getPoints())
        .object("bounties", tracker.getBounties())
        .created(DateTime.now())
        .build();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value("action", Action.GAME.getValueString())
        .value("points", String.valueOf(tracker.getPoints()))
        .value("badges", String.valueOf(tracker.getBounties()))
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
