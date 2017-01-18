package com.yoloo.backend.topic;

import com.google.common.base.Optional;

import com.googlecode.objectify.Key;

import io.reactivex.Single;
import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public class TopicService {

    public Topic create(Key<Topic> categoryKey, String name, Topic.Type type) {
        return Topic.builder()
                .id(categoryKey.getId())
                .name(name)
                .type(type)
                .rank(0)
                .build();
    }

    public Single<Topic> update(Topic topic, Optional<String> name,
                                Optional<Topic.Type> type) {
        if (name.isPresent()) {
            topic = topic.withName(name.get());
        }

        if (type.isPresent()) {
            topic = topic.withType(type.get());
        }

        return Single.just(topic);
    }
}
