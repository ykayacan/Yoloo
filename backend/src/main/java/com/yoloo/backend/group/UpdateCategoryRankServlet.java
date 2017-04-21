package com.yoloo.backend.group;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import io.reactivex.Observable;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class UpdateCategoryRankServlet extends HttpServlet {

  private static final String UPDATE_CATEGORY_RANK_QUEUE = "update-category-rank-queue";
  private static final String URL = "/tasks/update/category/rank";

  private final TravelerGroupShardService travelerGroupShardService = TravelerGroupShardService.create();

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
    Observable.fromIterable(ofy().load().type(TravelerGroupEntity.class).list())
        .flatMap(travelerGroupShardService::mergeShards)
        .toList()
        .subscribe(categories -> ofy().save().entities(categories));
  }
}
