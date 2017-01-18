package com.yoloo.backend.topic;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.cmd.Query;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class TopicRankUpdateServlet extends HttpServlet {

  public static final String UPDATE_CATEGORY_RANK_QUEUE = "update-topic-rank-queue";
  private static final String URL = "/tasks/update/topic/rank";

  public static void create() {
    Queue queue = QueueFactory.getQueue(UPDATE_CATEGORY_RANK_QUEUE);
    queue.addAsync(TaskOptions.Builder.withUrl(URL));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    updateRank();
  }

  private void updateRank() {
    // Init query fetch request.
    Query<Topic> query = ofy().load().type(Topic.class);

    List<Topic> topics = query.list();

    topics = TopicUtil.mergeCounts(topics)
        .toList(topics.size())
        .blockingGet();

    ofy().save().entities(topics);
  }
}
