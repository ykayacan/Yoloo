package com.yoloo.backend.hashtag;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HashTag {

    public static final String FIELD_GROUP_KEYS = "groupKeys";
    public static final String FIELD_NAME = "name";

    @Id
    private long id;

    @Index
    @NonFinal
    private String name;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private List<Key<HashTagCounterShard>> shardKeys;

    @Index
    @NonFinal
    private String language;

    @Index
    @NonFinal
    private long questions;

    @Index
    @NonFinal
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private List<Key<HashTagGroup>> groupKeys;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<HashTag> getKey() {
        return Key.create(HashTag.class, id);
    }

    @JsonProperty("id")
    public String websafeHashTagId() {
        return getKey().toWebSafeString();
    }

    public List<String> getGroupIds() {
        return Observable.fromIterable(groupKeys)
                .map(new Function<Key<HashTagGroup>, String>() {
                    @Override
                    public String apply(Key<HashTagGroup> groupKey) throws Exception {
                        return groupKey.toWebSafeString();
                    }
                })
                .toList()
                .blockingGet();
    }
}