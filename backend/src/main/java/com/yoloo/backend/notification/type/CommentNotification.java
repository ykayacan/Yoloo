package com.yoloo.backend.notification.type;

import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

@RequiredArgsConstructor(staticName = "create")
public class CommentNotification implements NotificationBundle {

  @NonNull private Account sender;

  @NonNull private Key<Account> receiverKey;

  @NonNull private DeviceRecord record;

  @NonNull private Comment comment;

  @Override public Notification getNotification() {
    return Notification.builder()
        .senderKey(sender.getKey())
        .receiverKey(receiverKey)
        .senderUsername(sender.getUsername())
        .senderAvatarUrl(sender.getAvatarUrl())
        .action(Action.COMMENT)
        .object("message", CommentUtil.trimmedContent(comment, 50))
        .object("questionKey", comment.getQuestionKey())
        .created(DateTime.now())
        .build();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value("action", Action.COMMENT.getValueString())
        .value("questionId", comment.getQuestionKey().toWebSafeString())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }
}
