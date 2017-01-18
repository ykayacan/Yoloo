package com.yoloo.backend.notification.type;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import com.yoloo.backend.question.Question;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

@RequiredArgsConstructor(staticName = "create")
public class AcceptNotification implements NotificationBundle {

  @NonNull private Key<Account> receiverKey;

  @NonNull private DeviceRecord record;

  @NonNull private Question question;

  @Override public Notification getNotification() {
    return Notification.builder()
        .senderKey(question.getParentUserKey())
        .receiverKey(receiverKey)
        .action(Action.ACCEPT)
        .object("questionKey", question.getKey())
        .created(DateTime.now())
        .build();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value("action", Action.ACCEPT.getValueString())
        .value("questionId", question.getWebsafeId())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
