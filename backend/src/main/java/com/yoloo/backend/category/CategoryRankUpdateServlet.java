package com.yoloo.backend.category;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.googlecode.objectify.Key;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class CategoryRankUpdateServlet extends HttpServlet {

    public static final String UPDATE_CATEGORY_RANK_QUEUE = "update-category-rank-queue";
    private static final String URL = "/tasks/update/category/rank";

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
        CategoryShardService service = CategoryShardService.newInstance();

        QueryResultIterable<Key<Category>> categoryKeysIterable =
                ofy().load().type(Category.class).keys().iterable();

        //ImmutableSet<Key<CategoryCounterShard>> shardKeys = service.getShardKeys();

    }
}
