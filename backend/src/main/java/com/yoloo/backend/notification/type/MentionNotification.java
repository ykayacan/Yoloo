package com.yoloo.backend.notification.type;

import com.google.common.collect.Lists;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.MessageConstants;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import com.yoloo.backend.question.Question;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class MentionNotification implements NotificationBundle {

  private Question question;
  private Account sender;
  private Collection<DeviceRecord> records;
  private Comment comment;

  @Override
  public List<Notification> getNotifications() {
    List<Notification> notifications = Lists.newArrayListWithCapacity(records.size());
    for (final DeviceRecord record : records) {
      Notification notification = Notification.builder()
          .senderKey(sender.getKey())
          .receiverKey(record.getParentUserKey())
          .senderId(sender.getWebsafeId())
          .senderUsername(sender.getUsername())
          .senderAvatarUrl(sender.getAvatarUrl())
          .action(Action.MENTION)
          .object("comment", CommentUtil.trimmedContent(comment, 50))
          .object("questionId", comment.getQuestionKey().toWebSafeString())
          .created(DateTime.now())
          .build();

      notifications.add(notification);
    }
    return notifications;
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(MessageConstants.ACTION, Action.MENTION.getValueString())
        .value(MessageConstants.QUESTION_ID, comment.getQuestionKey().toWebSafeString())
        .value(MessageConstants.SENDER_USERNAME, sender.getUsername())
        .value(MessageConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .value(MessageConstants.COMMENT, CommentUtil.trimmedContent(comment, 50))
        .value(MessageConstants.ACCEPTED_ID, question.getAcceptedCommentId())
        .build();

    return PushMessage.builder()
        .registrationIds(convertRegistrationIdsToList())
        .data(dataBody)
        .build();
  }

  private List<String> convertRegistrationIdsToList() {
    return Observable.fromIterable(records)
        .map(DeviceRecord::getRegId)
        .toList()
        .blockingGet();
  }
}
