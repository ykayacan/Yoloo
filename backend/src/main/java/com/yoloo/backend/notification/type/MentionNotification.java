package com.yoloo.backend.notification.type;

import com.google.common.collect.Lists;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
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
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class MentionNotification implements Notifiable {

  private PostEntity postEntity;
  private Account sender;
  private Collection<DeviceRecord> records;
  private Comment comment;

  @Override public List<Notification> getNotifications() {
    List<Notification> notifications = Lists.newArrayListWithCapacity(records.size());
    for (final DeviceRecord record : records) {
      Notification notification = Notification.builder()
          .senderKey(sender.getKey())
          .receiverKey(record.getParent())
          .senderUsername(sender.getUsername())
          .senderAvatarUrl(sender.getAvatarUrl())
          .action(Action.MENTION)
          .payload("comment", CommentUtil.trimContent(comment.getContent(), 50))
          .payload("postId", comment.getPostKey().toWebSafeString())
          .created(DateTime.now())
          .build();

      notifications.add(notification);
    }
    return notifications;
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(PushConstants.ACTION, Action.MENTION.getValueString())
        .value(PushConstants.QUESTION_ID, comment.getPostKey().toWebSafeString())
        .value(PushConstants.SENDER_USERNAME, sender.getUsername())
        .value(PushConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .value(PushConstants.COMMENT, CommentUtil.trimContent(comment.getContent(), 50))
        .value(PushConstants.ACCEPTED_ID, postEntity.getAcceptedCommentId())
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
