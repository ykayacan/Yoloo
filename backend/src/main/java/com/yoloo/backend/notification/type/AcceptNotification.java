package com.yoloo.backend.notification.type;

import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.post.Post;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class AcceptNotification implements NotificationBundle {

  private Account sender;
  private DeviceRecord record;
  private Post post;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification.builder()
        .senderKey(sender.getKey())
        .receiverKey(record.getParent())
        .action(Action.ACCEPT)
        .payload("questionId", post.getWebsafeId())
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(PushConstants.ACTION, Action.ACCEPT.getValueString())
        .value(PushConstants.SENDER_USERNAME, sender.getWebsafeId())
        .value(PushConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .value(PushConstants.QUESTION_ID, post.getWebsafeId())
        .value(PushConstants.ACCEPTED_ID, post.getAcceptedCommentId())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
