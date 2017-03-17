package com.yoloo.backend.account.task;

import com.annimon.stream.Stream;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.category.Category;
import com.yoloo.backend.feed.Feed;
import com.yoloo.backend.post.Post;
import com.yoloo.backend.util.KeyUtil;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class CreateUserFeedServlet extends HttpServlet {

  private static final Logger LOG =
      Logger.getLogger(CreateUserFeedServlet.class.getName());

  private static final String CREATE_FEED_QUEUE = "create-feed-queue";
  private static final String URL = "/tasks/create/feed";

  private static final String ACCOUNT_ID = "accountId";
  private static final String CATEGORY_IDS = "categoryIds";

  public static void addToQueue(String accountId, String categoryIds) {
    LOG.info("New task is added to queue.");

    Queue queue = QueueFactory.getQueue(CREATE_FEED_QUEUE);
    queue.add(TaskOptions.Builder
        .withUrl(URL)
        .param(ACCOUNT_ID, accountId)
        .param(CATEGORY_IDS, categoryIds));
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    processRequest(req, resp);
  }

  private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    final String accountId = req.getParameter(ACCOUNT_ID);
    final String categoryIds = req.getParameter(CATEGORY_IDS);

    LOG.info("accountId: " + accountId);
    LOG.info("CategoryIds: " + categoryIds);

    List<Key<Category>> keys = KeyUtil.extractKeysFromIds(categoryIds, ",");

    Query<Post> query = ofy().load().type(Post.class);

    for (Key<Category> key : keys) {
      query = query.filter(Post.FIELD_CATEGORIES + " =", Category.extractNameFromKey(key));
    }

    List<Key<Post>> postKeys = query.order("-" + Post.FIELD_CREATED)
        .limit(getRequiredEntitySize(keys))
        .keys().list();

    List<Feed> feeds = Stream.of(postKeys)
        .map(postKey -> Feed.builder()
            .id(Feed.createId(postKey))
            .parent(Key.create(accountId))
            .build())
        .toList();

    LOG.info("Feeds: " + feeds);

    ofy().save().entities(feeds);
  }

  private int getRequiredEntitySize(List<Key<Category>> keys) {
    final int size = keys.size();
    return size > 3 ? 25 : 35;
  }
}
