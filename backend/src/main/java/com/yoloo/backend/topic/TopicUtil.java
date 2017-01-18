package com.yoloo.backend.topic;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicUtil {

  public static Observable<Topic> mergeCounts(Topic topic) {
    return mergeCounts(Lists.newArrayList(topic));
  }

  public static Observable<Topic> mergeCounts(Collection<Topic> topics) {
    if (topics.isEmpty()) return Observable.empty();

    return Observable.fromIterable(topics).flatMap(TopicUtil::mergeShards);
  }

  private static Observable<Topic> mergeShards(Topic topic) {
    return Observable.fromIterable(topic.getShards())
        .cast(TopicCounterShard.class)
        .reduce(TopicUtil::reduceCounters)
        .map(shard -> mapToTopic(topic, shard))
        .toObservable();
  }

  private static TopicCounterShard reduceCounters(TopicCounterShard s1, TopicCounterShard s2) {
    return s1.addQuestions(s2.getQuestions());
  }

  private static Topic mapToTopic(Topic topic, TopicCounterShard shard) {
    return topic.withQuestions(shard.getQuestions());
  }
}
