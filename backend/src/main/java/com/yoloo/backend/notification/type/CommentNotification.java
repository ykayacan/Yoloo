package com.yoloo.backend.notification.type;

import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
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
public class CommentNotification implements NotificationBundle {

  private Account sender;

  private DeviceRecord record;

  private Comment comment;

  private Post post;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification.builder()
        .senderKey(sender.getKey())
        .receiverKey(record.getParent())
        .senderUsername(sender.getUsername())
        .senderAvatarUrl(sender.getAvatarUrl())
        .action(Action.COMMENT)
        .payload("comment", CommentUtil.trimmedContent(comment, 50))
        .payload("questionId", comment.getQuestionKey().toWebSafeString())
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value(PushConstants.ACTION, Action.COMMENT.getValueString())
        .value(PushConstants.QUESTION_ID, comment.getQuestionKey().toWebSafeString())
        .value(PushConstants.SENDER_USERNAME, sender.getUsername())
        .value(PushConstants.SENDER_AVATAR_URL, sender.getAvatarUrl().getValue())
        .value(PushConstants.ACCEPTED_ID, post.getAcceptedCommentId())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
