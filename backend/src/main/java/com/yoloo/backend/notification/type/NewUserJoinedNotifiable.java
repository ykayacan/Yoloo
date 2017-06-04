package com.yoloo.backend.notification.type;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import ix.Ix;
import java.util.Collections;
import java.util.List;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(staticName = "create")
public class NewUserJoinedNotifiable implements Notifiable {

  @Override public List<Notification> getNotifications() {
    return Collections.emptyList();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody
        .builder()
        .value(PushConstants.ACTION, Action.USER_JOINED.getValueString())
        .build();

    return PushMessage.builder()
        .registrationIds(getRegIds())
        .data(dataBody)
        .build();
  }

  private List<String> getRegIds() {
    return Ix.from(ofy().load().type(DeviceRecord.class).list())
        .map(DeviceRecord::getRegId)
        .toList();
  }
}
