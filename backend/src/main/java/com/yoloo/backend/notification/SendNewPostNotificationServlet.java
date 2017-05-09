package com.yoloo.backend.notification;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.type.NewFriendPostNotification;
import com.yoloo.backend.notification.type.Notifiable;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.relationship.Relationship;
import ix.Ix;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class SendNewPostNotificationServlet extends HttpServlet {

  public static final String UPDATE_FEED_QUEUE = "send-new-post-queue";
  private static final String URL = "/tasks/send/notification";

  private static final String USER_ID = "userId";
  private static final String POST_ID = "postId";

  public static void addToQueue(@Nonnull String userId, @Nonnull String postId) {
    Queue queue = QueueFactory.getQueue(UPDATE_FEED_QUEUE);
    queue.add(TaskOptions.Builder.withUrl(URL).param(USER_ID, userId).param(POST_ID, postId));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String accountId = req.getParameter(USER_ID);
    final String postId = req.getParameter(POST_ID);

    Ix.from(findFollowersOfUser(Key.create(accountId)))
        .map(Key::<Account>getParent)
        .collectToList()
        .doOnNext(accountKeys -> {
          List<Key<?>> keyList = new ArrayList<>();

          for (Key<Account> accountKey : accountKeys) {
            keyList.add(DeviceRecord.createKey(accountKey));
          }

          Key<PostEntity> postKey = Key.create(postId);
          keyList.add(postKey);

          Map<Key<Object>, Object> map =
              ofy().load().keys(accountKeys.toArray(new Key[keyList.size()]));

          List<DeviceRecord> deviceRecords = new ArrayList<>();
          for (Key<Account> accountKey : accountKeys) {
            deviceRecords.add((DeviceRecord) map.get(DeviceRecord.createKey(accountKey)));
          }

          PostEntity post = (PostEntity) map.get(postKey);

          Notifiable notifiable = NewFriendPostNotification.create(post, deviceRecords);

          NotificationService service =
              NotificationService.create(URLFetchServiceFactory.getURLFetchService());

          service.send(notifiable);
        });
  }

  private List<Key<Relationship>> findFollowersOfUser(Key<Account> accountKey) {
    return ofy()
        .load()
        .type(Relationship.class)
        .filter(Relationship.FIELD_FOLLOWING_KEY + " =", accountKey)
        .keys()
        .list();
  }
}
