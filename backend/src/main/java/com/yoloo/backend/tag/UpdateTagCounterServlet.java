package com.yoloo.backend.tag;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import io.reactivex.Observable;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class UpdateTagCounterServlet extends HttpServlet {

  private static final String UPDATE_HASHTAG_COUNTER_QUEUE = "update-tag-counter-queue";
  private static final String URL = "/tasks/update/tag/counter";

  public static void create() {
    Queue queue = QueueFactory.getQueue(UPDATE_HASHTAG_COUNTER_QUEUE);
    queue.addAsync(TaskOptions.Builder.withUrl(URL));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    List<Tag> tags = ofy().load().type(Tag.class).list();

    List<Tag> modified = Observable.fromIterable(tags)
        .map(tag -> Observable.fromIterable(tag.getShards())
            .reduce((s1, s2) -> s1.withPosts(s1.getPosts() + s2.getPosts()))
            .map(shard -> tag.withPosts(shard.getPosts()))
            .blockingGet())
        .toList(tags.size())
        .blockingGet();

    ofy().save().entities(modified);
  }
}
