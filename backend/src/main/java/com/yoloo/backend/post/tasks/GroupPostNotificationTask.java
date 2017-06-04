package com.yoloo.backend.post.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.GroupPostNotification;
import ix.Ix;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
public class GroupPostNotificationTask extends HttpServlet {

  private static final String QUEUE_NAME = "new-post-notification-queue";
  private static final String URL = "/tasks/send/notification";

  private static final String POST_ID = "postId";
  private static final String GROUP_ID = "groupId";

  private final NotificationService notificationService =
      NotificationService.create(URLFetchServiceFactory.getURLFetchService());

  public static void addToQueue(@Nonnull String postId, @Nonnull String groupId) {
    Queue queue = QueueFactory.getQueue(QUEUE_NAME);
    queue.addAsync(TaskOptions.Builder.withUrl(URL)
        .param(POST_ID, postId)
        .param(GROUP_ID, groupId));
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String postId = req.getParameter(POST_ID);
    final String groupId = req.getParameter(GROUP_ID);

    final Key<TravelerGroupEntity> groupKey = Key.create(groupId);

    Collection<DeviceRecord> records = Ix.from(findSubscribers(groupKey))
        .map(Key::<Account>getParent)
        .map(DeviceRecord::createKey)
        .collectToList()
        .map(keys -> ofy().load().keys(keys).values())
        .single();

    notificationService.send(
        GroupPostNotification.create(postId, TravelerGroupEntity.extractNameFromKey(groupKey),
            records));
  }

  private Iterable<Key<Account>> findSubscribers(Key<TravelerGroupEntity> groupKey) {
    return ofy()
        .load()
        .type(Account.class)
        .filter(Account.FIELD_SUBSCRIBED_GROUP_KEYS, groupKey)
        .keys()
        .iterable();
  }
}
