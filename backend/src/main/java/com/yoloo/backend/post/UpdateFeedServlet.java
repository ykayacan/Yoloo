package com.yoloo.backend.post;

import com.annimon.stream.Stream;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.relationship.Relationship;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class UpdateFeedServlet extends HttpServlet {

  public static final String UPDATE_FEED_QUEUE = "update-feed-queue";
  private static final String URL = "/tasks/update/feed";

  private static final String ACCOUNT_ID = "accountId";
  private static final String POST_ID = "postId";
  private static final String CREATED = "created";

  public static void addToQueue(String accountId, String postId) {
    Queue queue = QueueFactory.getQueue(UPDATE_FEED_QUEUE);
    queue.add(TaskOptions.Builder
        .withUrl(URL)
        .param(ACCOUNT_ID, accountId)
        .param(POST_ID, postId));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String accountId = req.getParameter(ACCOUNT_ID);
    final String postId = req.getParameter(POST_ID);

    final Key<Account> accountKey = Key.create(accountId);
    final Key<Post> postKey = Key.create(postId);

    List<Feed> feeds = Stream.of(findFollowersOfUser(accountKey))
        .map(Key::<Account>getParent)
        .map(followerKey -> createFeed(followerKey, postKey))
        .toList();

    // Add to user's own feed.
    feeds.add(createFeed(accountKey, postKey));

    ofy().save().entities(feeds).now();
  }

  private List<Key<Relationship>> findFollowersOfUser(Key<Account> accountKey) {
    return ofy().load().type(Relationship.class)
        .filter(Relationship.FIELD_FOLLOWING_KEY + " =", accountKey)
        .keys().list();
  }

  private Feed createFeed(Key<Account> followerKey, Key<Post> postKey) {
    return Feed.builder()
        .id(Feed.createId(postKey))
        .parent(followerKey)
        .build();
  }
}
