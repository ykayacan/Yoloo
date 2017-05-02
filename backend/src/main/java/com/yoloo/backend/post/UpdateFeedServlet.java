package com.yoloo.backend.post;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.relationship.Relationship;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ix.Ix;

import static com.yoloo.backend.OfyService.ofy;

public class UpdateFeedServlet extends HttpServlet {

  public static final String UPDATE_FEED_QUEUE = "update-feed-queue";
  private static final String URL = "/tasks/update/feed";

  private static final String USER_ID = "userId";
  private static final String POST_ID = "postId";

  public static void addToQueue(@Nonnull String userId, @Nonnull String postId) {
    Queue queue = QueueFactory.getQueue(UPDATE_FEED_QUEUE);
    queue.add(TaskOptions.Builder
        .withUrl(URL)
        .param(USER_ID, userId)
        .param(POST_ID, postId));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String accountId = req.getParameter(USER_ID);
    final String postId = req.getParameter(POST_ID);

    final Key<Account> accountKey = Key.create(accountId);
    final Key<PostEntity> postKey = Key.create(postId);

    List<Feed> feeds = Ix.from(findFollowersOfUser(accountKey))
        .map(Key::<Account>getParent)
        .concatWith(Collections.singletonList(accountKey))
        .map(followerKey -> createFeed(followerKey, postKey))
        .toList();

    ofy().save().entities(feeds);
  }

  private List<Key<Relationship>> findFollowersOfUser(Key<Account> accountKey) {
    return ofy().load().type(Relationship.class)
        .filter(Relationship.FIELD_FOLLOWING_KEY + " =", accountKey)
        .keys().list();
  }

  private Feed createFeed(Key<Account> followerKey, Key<PostEntity> postKey) {
    return Feed.builder()
        .id(Feed.createId(postKey))
        .parent(followerKey)
        .post(Ref.create(postKey))
        .build();
  }
}
