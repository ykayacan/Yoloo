package com.yoloo.backend.notification.type;

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.comment.Comment;
import com.yoloo.backend.comment.CommentUtil;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.Notification;
import com.yoloo.backend.notification.PushMessage;
import com.yoloo.backend.notification.action.Action;
import com.yoloo.backend.util.MentionHelper;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class MentionNotification implements NotificationBundle {

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
        .action(Action.MENTION)
        .object("message", CommentUtil.trimmedContent(comment, 50))
        .object("questionKey", comment.getQuestionKey())
        .created(DateTime.now())
        .build();
  }

  @Override public PushMessage getPushMessage() {
    PushMessage.DataBody dataBody = PushMessage.DataBody.builder()
        .value("action", Action.MENTION.getValueString())
        .value("questionId", comment.getQuestionKey().toWebSafeString())
        .build();

    return PushMessage.builder()
        .to(record.getRegId())
        .data(dataBody)
        .build();
  }

  public Optional<List<Account>> getMentionedAccounts(String content) {
    List<String> mentionedUsernames = MentionHelper.getMentions(content);

    if (!mentionedUsernames.isEmpty()) {
      Query<Account> query = ofy().load().type(Account.class);

      for (String username : mentionedUsernames) {
        query = query.filter(Account.FIELD_USERNAME + " =", username);
      }

      return Optional.of(query.project(Account.FIELD_USERNAME).list());
    }

    return Optional.absent();
  }
}
