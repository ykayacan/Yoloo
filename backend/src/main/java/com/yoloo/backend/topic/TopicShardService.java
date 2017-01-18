package com.yoloo.backend.topic;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.yoloo.backend.question.QuestionWrapper;
import com.yoloo.backend.shard.ShardService;
import com.yoloo.backend.shard.ShardUtil;
import com.yoloo.backend.util.StringUtil;
import io.reactivex.Observable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.NoArgsConstructor;

import static com.yoloo.backend.OfyService.ofy;

@NoArgsConstructor(staticName = "create")
public class TopicShardService implements ShardService<Topic, TopicCounterShard> {

  @Override
  public List<Key<TopicCounterShard>> createShardKeys(Iterable<Key<Topic>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShardKeys)
        .toList()
        .blockingGet();
  }

  @Override
  public List<Key<TopicCounterShard>> createShardKeys(final Key<Topic> entityKey) {
    return Observable
        .range(1, TopicCounterShard.SHARD_COUNT)
        .map(id -> TopicCounterShard.createKey(entityKey, id))
        .toList()
        .blockingGet();
  }

  @Override
  public List<TopicCounterShard> createShards(Iterable<Key<Topic>> keys) {
    return Observable
        .fromIterable(keys)
        .concatMapIterable(this::createShards)
        .toList()
        .blockingGet();
  }

  @Override
  public List<TopicCounterShard> createShards(final Key<Topic> entityKey) {
    return Observable
        .range(1, TopicCounterShard.SHARD_COUNT)
        .map(shardId -> createShard(entityKey, shardId))
        .toList()
        .blockingGet();
  }

  @Override
  public TopicCounterShard createShard(Key<Topic> entityKey, int shardId) {
    return TopicCounterShard.builder()
        .id(ShardUtil.generateShardId(entityKey, shardId))
        .questions(0)
        .build();
  }

  @Override
  public Key<TopicCounterShard> getRandomShardKey(Key<Topic> entityKey) {
    final int shardNum = new Random().nextInt(TopicCounterShard.SHARD_COUNT - 1 + 1) + 1;
    return TopicCounterShard.createKey(entityKey, shardNum);
  }

  public Collection<TopicCounterShard> updateShards(QuestionWrapper wrapper) {
    Set<String> categories = StringUtil.splitToSet(wrapper.getTopics(), ",");
    return updateShards(categories);
  }

  public Collection<TopicCounterShard> updateShards(Iterable<String> categories) {
    Query<Topic> query = ofy().load().type(Topic.class);

    for (String category : categories) {
      query = query.filter(Topic.FIELD_NAME + " =", category);
    }

    List<Key<Topic>> categoryKeys = query.keys().list();

    List<Key<TopicCounterShard>> categoryShardKeys =
        Lists.newArrayListWithCapacity(categoryKeys.size());

    for (Key<Topic> key : categoryKeys) {
      categoryShardKeys.add(getRandomShardKey(key));
    }

    Map<Key<TopicCounterShard>, TopicCounterShard> map = ofy().load().keys(categoryShardKeys);

    for (TopicCounterShard ccs : map.values()) {
      ccs.increaseQuestions();

      map.put(ccs.getKey(), ccs);
    }

    return map.values();
  }
}
