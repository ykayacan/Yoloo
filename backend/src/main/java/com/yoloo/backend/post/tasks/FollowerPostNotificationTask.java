package com.yoloo.backend.post.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.device.DeviceRecord;
import com.yoloo.backend.notification.NotificationService;
import com.yoloo.backend.notification.type.FollowerPostNotification;
import com.yoloo.backend.relationship.Relationship;
import ix.Ix;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class FollowerPostNotificationTask extends HttpServlet {

  private static final String QUEUE_NAME = "new-post-notification-queue";
  private static final String URL = "/tasks/send/notification";

  private static final String USER_ID = "userId";
  private static final String POST_ID = "postId";
  private static final String USERNAME = "username";

  private final NotificationService notificationService =
      NotificationService.create(URLFetchServiceFactory.getURLFetchService());

  public static void addToQueue(@Nonnull String userId, @Nonnull String postId,
      @Nonnull String username) {
    Queue queue = QueueFactory.getQueue(QUEUE_NAME);
    queue.addAsync(TaskOptions.Builder.withUrl(URL)
        .param(USER_ID, userId)
        .param(POST_ID, postId)
        .param(USERNAME, username));
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String accountId = req.getParameter(USER_ID);
    final String postId = req.getParameter(POST_ID);
    final String username = req.getParameter(USERNAME);

    Collection<DeviceRecord> records = Ix.from(findFollowersOfUser(Key.create(accountId)))
        .map(Key::<Account>getParent)
        .map(DeviceRecord::createKey)
        .collectToList()
        .map(keys -> ofy().load().keys(keys).values())
        .single();

    notificationService.send(FollowerPostNotification.create(postId, username, records));
  }

  private Iterable<Key<Relationship>> findFollowersOfUser(Key<Account> accountKey) {
    return ofy()
        .load()
        .type(Relationship.class)
        .filter(Relationship.FIELD_FOLLOWING_KEY, accountKey)
        .keys()
        .iterable();
  }
}
