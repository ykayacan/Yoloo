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

        int index = 0;
        for (Tag tag : tags) {
            long questions = 0;

            for (TagCounterShard shard : tag.getShards()) {
                questions += shard.getQuestions();
            }

            tags.add(index, tag.withQuestions(questions));
            index++;
        }



        ofy().save().entities(tags);

        /*TagShardService service = TagShardService.newInstance();

        Map<Key<TagCounterShard>, TagCounterShard> map =
                ofy().load().keys(service.getShardKeys(tagEntities));

        int index = 0;
        for (Tag hashTag : tagEntities) {
            long questions = 0L;

            for (int i = 1; i <= TagCounterShard.SHARD_COUNT; i++) {
                Key<TagCounterShard> shardKey = hashTag.getShardKeys().get(i - 1);

                if (map.containsKey(shardKey)) {
                    TagCounterShard shard = map.get(shardKey);
                    questions += shard.getQuestions();
                }
            }

            tagEntities.addAdmin(index, hashTag.withQuestions(questions));
            index++;
        }

        ofy().save().entities(tagEntities);*/
    }
}
