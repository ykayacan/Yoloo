package com.yoloo.backend.account.task;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.group.TravelerGroupEntity;
import com.yoloo.backend.post.PostEntity;
import com.yoloo.backend.util.KeyUtil;
import ix.Ix;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

import static com.yoloo.backend.OfyService.ofy;

@Log
public class CreateUserFeedServlet extends HttpServlet {

  private static final String CREATE_FEED_QUEUE = "create-feed-queue";
  private static final String URL = "/tasks/create/feed";

  private static final String USER_ID = "userId";
  private static final String TRAVEL_GROUP_IDS = "travelGroupIds";

  public static void addToQueue(String accountId, String groupIds) {
    Queue queue = QueueFactory.getQueue(CREATE_FEED_QUEUE);
    queue.addAsync(TaskOptions.Builder
        .withUrl(URL)
        .param(USER_ID, accountId)
        .param(TRAVEL_GROUP_IDS, groupIds));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String userId = req.getParameter(USER_ID);
    final String stringifiedSubscribedIds = req.getParameter(TRAVEL_GROUP_IDS);

    List<Key<TravelerGroupEntity>> groupKeys =
        KeyUtil.extractKeysFromIds(stringifiedSubscribedIds, ",");

    Query<PostEntity> query = ofy().load().type(PostEntity.class);

    for (Key<TravelerGroupEntity> key : groupKeys) {
      query = query.filter(PostEntity.FIELD_GROUP_KEY + " =", key);
    }

    List<Key<PostEntity>> postKeys =
        query.order("-" + PostEntity.FIELD_CREATED).limit(100).keys().list();

    List<Feed> feeds = Ix.from(postKeys).map(postKey -> getFeed(userId, postKey)).toList();

    ofy().save().entities(feeds);
  }

  private Feed getFeed(String userId, Key<PostEntity> postKey) {
    return Feed.builder().id(Feed.createId(postKey)).parent(Key.create(userId)).build();
  }
}
