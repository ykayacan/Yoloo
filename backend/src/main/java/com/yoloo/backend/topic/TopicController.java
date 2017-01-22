package com.yoloo.backend.topic;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.topic.sort_strategy.CategorySorter;
import io.reactivex.Single;
import java.util.List;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.yoloo.backend.OfyService.factory;
import static com.yoloo.backend.OfyService.ofy;

@RequiredArgsConstructor(staticName = "create")
public class TopicController extends Controller {

  private static final Logger logger =
      Logger.getLogger(TopicController.class.getName());

  /**
   * Maximum number of categories to return.
   */
  private static final int DEFAULT_LIST_LIMIT = 7;

  @NonNull
  private TopicService topicService;

  @NonNull
  private TopicShardService topicShardService;

  public Topic add(String name, Topic.Type type, User user) throws ConflictException {
    /*Key<Topic> savedKey = ofy().load().type(Topic.class)
        .filter(Topic.FIELD_NAME + " =", name).keys().first().now();*/

    //Guard.checkConflictRequest(savedKey, name + " category exists.");

    Key<Topic> topicKey = factory().allocateId(Topic.class);

    List<TopicCounterShard> shards = topicShardService.createShards(topicKey);

    List<Ref<TopicCounterShard>> shardRefs =
        ShardUtil.createRefs(shards).toList().blockingGet();

    Topic topic = topicService
        .create(topicKey, name, type)
        .withShardRefs(shardRefs);

    ImmutableSet<Object> saveList = ImmutableSet.builder()
        .add(topic)
        .addAll(shards)
        .build();

    ofy().save().entities(saveList).now();

    return topic;
  }

  public Topic update(String topicId, final Optional<String> name, Optional<Topic.Type> type,
      User user) {
    return Single.just(topicId)
        .flatMap(s -> {
          Topic topic = ofy().load().key(Key.<Topic>create(s)).now();

          return topicService.update(topic, name, type);
        })
        .doOnSuccess(topic -> ofy().save().entity(topic).now())
        .blockingGet();
  }

  /**
   * List collection response.
   *
   * @param sorter the sorter
   * @param limit the limit
   * @param cursor the cursor
   * @return the collection response
   */
  public CollectionResponse<Topic> list(Optional<CategorySorter> sorter, Optional<Integer> limit,
      Optional<String> cursor) {
    // If sorter parameter is null, default sort strategy is "DEFAULT".
    CategorySorter categorySorter = sorter.or(CategorySorter.DEFAULT);

    // Init query fetch request.
    Query<Topic> query = ofy().load().type(Topic.class);

    // Sort by category sorter then edit query.
    query = CategorySorter.sort(query, categorySorter);

    // Fetch items from beginning from cursor.
    query = cursor.isPresent()
        ? query.startAt(Cursor.fromWebSafeString(cursor.get()))
        : query;

    // Limit items.
    query = query.limit(limit.or(DEFAULT_LIST_LIMIT));

    final QueryResultIterator<Topic> qi = query.iterator();

    List<Topic> topics = Lists.newArrayListWithCapacity(DEFAULT_LIST_LIMIT);

    while (qi.hasNext()) {
      // Add fetched objects to map. Because cursor iteration needs to be iterated.
      topics.add(qi.next());
    }

    /*topics = TopicUtil.mergeCounts(topics)
        .toList(DEFAULT_LIST_LIMIT)
        .blockingGet();*/

    return CollectionResponse.<Topic>builder()
        .setItems(topics)
        .setNextPageToken(qi.getCursor().toWebSafeString())
        .build();
  }
}
