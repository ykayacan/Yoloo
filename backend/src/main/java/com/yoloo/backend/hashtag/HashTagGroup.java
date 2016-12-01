package com.yoloo.backend.hashtag;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

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
public class HashTagGroup {

    public static final String FIELD_NAME = "name";

    @Id
    private Long id;

    @Index
    @NonFinal
    private String name;

    /**
     * Total number of hashTags in the group.
     *
     * Updated in a given interval.
     */
    @Index
    @NonFinal
    private long totalHashTagCount;

    /**
     * Total number of questions in hashTag group.
     *
     * Updated in a given interval.
     */
    @Index
    @NonFinal
    private long totalQuestionCount;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<HashTagGroup> getKey() {
        return Key.create(HashTagGroup.class, id);
    }

    @JsonProperty("id")
    public String getWebsafeId() {
        return getKey().toWebSafeString();
    }
}