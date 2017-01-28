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

public class TagCounterUpdateServlet extends HttpServlet {

  public static final String UPDATE_HASHTAG_COUNTER_QUEUE = "update-hashtag-counter-queue";
  private static final String URL = "/tasks/update/hashtag/counter";

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
            .reduce((s1, s2) -> s1.withQuestions(s1.getQuestions() + s2.getQuestions()))
            .map(shard -> tag.withQuestions(shard.getQuestions()))
            .blockingGet())
        .toList(tags.size())
        .blockingGet();

    /*final int size = tags.size();

    for (int i = size - 1; i >= 0; --i) {
      Tag tag = tags.get(i);
      long questions = 0;
      for (TagCounterShard shard : tag.getShards()) {
        questions += shard.getQuestions();
      }

      tags.add(i, tag.withQuestions(questions));
    }*/

    ofy().save().entities(modified);
  }
}
