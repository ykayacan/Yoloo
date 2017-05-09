package com.yoloo.backend.notification.type;

import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.post.PostEntity;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "create")
public class NewFriendPostNotification implements Notifiable {

  private PostEntity post;
  private Collection<DeviceRecord> records;

  @Override public List<Notification> getNotifications() {
    return null;
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody
        .builder()
        .value(PushConstants.ACTION, Action.NEW_FRIEND_POST.getValueString())
        .value(PushConstants.POST_ID, post.getWebsafeId())
        .value(PushConstants.SENDER_USERNAME, post.getUsername())
        .build();

    return PushMessage.builder()
        .registrationIds(convertRegistrationIdsToList())
        .data(dataBody)
        .build();
  }

  private List<String> convertRegistrationIdsToList() {
    return Observable.fromIterable(records).map(DeviceRecord::getRegId).toList().blockingGet();
  }
}
