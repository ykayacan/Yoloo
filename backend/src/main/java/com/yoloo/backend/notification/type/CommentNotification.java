package com.yoloo.backend.notification.type;

import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.MessageConstants;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import com.yoloo.backend.question.Question;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class CommentNotification implements NotificationBundle {

  private Account sender;

  private DeviceRecord record;

  private Comment comment;

  private Question question;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification.builder()
        .senderKey(sender.getKey())
        .receiverKey(record.getParentUserKey())
        .senderId(sender.getWebsafeId())
        .senderUsername(sender.getUsername())
        .senderAvatarUrl(sender.getAvatarUrl())
        .action(Action.COMMENT)
        .object("comment", CommentUtil.trimmedContent(comment, 50))
        .object("questionId", comment.getQuestionKey().toWebSafeString())
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(MessageConstants.ACTION, Action.COMMENT.getValueString())
        .value(MessageConstants.QUESTION_ID, comment.getQuestionKey().toWebSafeString())
        .value(MessageConstants.SENDER_USERNAME, sender.getUsername())
        .value(MessageConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .value(MessageConstants.ACCEPTED_ID, question.getAcceptedCommentId())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
