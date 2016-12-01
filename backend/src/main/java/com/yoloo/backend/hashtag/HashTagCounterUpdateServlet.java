package com.yoloo.backend.hashtag;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.googlecode.objectify.Key;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.backend.OfyService.ofy;

public class HashTagCounterUpdateServlet extends HttpServlet {

    public static final String UPDATE_HASHTAG_COUNTER_QUEUE = "update-hashtag-counter-queue";
    private static final String URL = "/tasks/update/hashtag/counter";

    public static void create() {
        Queue queue = QueueFactory.getQueue(UPDATE_HASHTAG_COUNTER_QUEUE);
        queue.addAsync(TaskOptions.Builder.withUrl(URL));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<HashTag> hashTags = ofy().load().type(HashTag.class).list();

        HashTagShardService service = HashTagShardService.newInstance();

        Map<Key<HashTagCounterShard>, HashTagCounterShard> map =
                ofy().load().keys(service.getShardKeys(hashTags));

        int index = 0;
        for (HashTag hashTag : hashTags) {
            long questions = 0L;

            for (int i = 1; i <= HashTagCounterShard.SHARD_COUNT; i++) {
                Key<HashTagCounterShard> shardKey = hashTag.getShardKeys().get(i - 1);

                if (map.containsKey(shardKey)) {
                    HashTagCounterShard shard = map.get(shardKey);
                    questions += shard.getQuestions();
                }
            }

            hashTags.add(index, hashTag.withQuestions(questions));
            index++;
        }

        ofy().save().entities(hashTags);
    }
}
