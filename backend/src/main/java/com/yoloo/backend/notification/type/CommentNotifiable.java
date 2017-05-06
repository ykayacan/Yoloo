package com.yoloo.backend.notification.type;

import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Action;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushConstants;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.post.PostEntity;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@AllArgsConstructor(staticName = "create")
public class CommentNotifiable implements Notifiable {

  private Account commenter;
  private DeviceRecord record;
  private Comment comment;
  private PostEntity postEntity;

  @Override
  public List<Notification> getNotifications() {
    Notification notification = Notification
        .builder()
        .senderKey(commenter.getKey())
        .receiverKey(record.getParent())
        .senderUsername(commenter.getUsername())
        .senderAvatarUrl(commenter.getAvatarUrl())
        .action(Action.COMMENT)
        .payload(PushConstants.COMMENT, CommentUtil.trimContent(comment.getContent(), 50))
        .payload(PushConstants.POST_ID, comment.getPostKey().toWebSafeString())
        .created(DateTime.now())
        .build();

    return Collections.singletonList(notification);
  }

  @Override
  public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody
        .builder()
        .value(PushConstants.ACTION, Action.COMMENT.getValueString())
        .value(PushConstants.POST_ID, comment.getPostKey().toWebSafeString())
        .value(PushConstants.SENDER_USERNAME, commenter.getUsername())
        .value(PushConstants.SENDER_AVATAR_URL, commenter.getAvatarUrl().getValue())
        .build();

    return PushMessage.builder().to(record.getRegId()).data(dataBody).build();
  }
}
