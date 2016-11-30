package com.yoloo.backend.category;

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
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Category {

    @Id
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Long id;

    private String name;

    @Index
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private double rank;

    // Extra fields

    private long questions;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Category> getKey() {
        return Key.create(Category.class, id);
    }

    @JsonProperty("id")
    private String getWebsafeId() {
        return getKey().toWebSafeString();
    }
}
