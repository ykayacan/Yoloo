package com.yoloo.backend.tag;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotNull;
import com.googlecode.objectify.condition.IfNotZero;
import com.yoloo.backend.util.Deref;
import io.reactivex.Observable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Tag {

  public static final String FIELD_GROUP_KEYS = "groupKeys";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_QUESTIONS = "questions";
  public static final String FIELD_TYPE = "type";

  @Id
  private Long id;

  @Load
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<TagCounterShard>> shardRefs;

  @Index
  @NonFinal
  private String name;

  @Index
  @NonFinal
  private Type type;

  @Index
  @NonFinal
  private String language;

  @Index
  @NonFinal
  private long questions;

  /**
   * Total number of Tags in the group.
   *
   * Updated in a given interval.
   */
  @Index(IfNotZero.class)
  @NonFinal
  private long totalTagCount;

  @Index(IfNotNull.class)
  @NonFinal
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Key<Tag>> groupKeys;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Tag> getKey() {
    return Key.create(Tag.class, id);
  }

  @JsonProperty("id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public List<String> getGroupIds() {
    return Observable.fromIterable(groupKeys)
        .map(Key::toWebSafeString)
        .toList()
        .blockingGet();
  }

  public List<TagCounterShard> getShards() {
    return Deref.deref(shardRefs);
  }

  public enum Type {
    NORMAL,
    GROUP
  }
}