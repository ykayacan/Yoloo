package com.yoloo.backend.tag;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class UpdateTagServlet extends HttpServlet {

  private static final String UPDATE_TAG_COUNTER_QUEUE = "update-tag-queue";
  private static final String URL = "/tasks/update/tag";

  public static void create() {
    Queue queue = QueueFactory.getQueue(UPDATE_TAG_COUNTER_QUEUE);
    queue.addAsync(TaskOptions.Builder.withUrl(URL));
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    List<Tag> tags = ofy().load().type(Tag.class).list();

    /*List<Tag> modified = Ix.from(tags)
        .map(tag -> Stream.of(tag.getShards())
            .reduce((s1, s2) -> s1.withPosts(s1.getPosts() + s2.getPosts()))
            .map(shard -> tag.withPostCount(shard.getPosts()))
            .get())
        .toList();*/

    //ofy().save().entities(modified);
  }
}
