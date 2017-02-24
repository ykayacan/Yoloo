package com.yoloo.backend.tag;

import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotZero;
import com.googlecode.objectify.condition.IfNull;
import com.yoloo.backend.tag.transformer.TagTransformer;
import com.yoloo.backend.util.Deref;
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
@ApiTransformer(TagTransformer.class)
public class Tag {

  public static final String FIELD_GROUP_KEYS = "groupKeys";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_QUESTIONS = "postCount";
  public static final String FIELD_TYPE = "type";

  @Id
  private Long id;

  @Load
  @NonFinal
  private List<Ref<TagShard>> shardRefs;

  @Index
  @NonFinal
  private String name;

  @Index
  @NonFinal
  private Type type;

  @Index
  @IgnoreSave(IfNull.class)
  @NonFinal
  private String langCode;

  @Index(IfNotZero.class)
  @NonFinal
  private long posts;

  /**
   * Total number of Tags in the group.
   *
   * Updated in a given interval.
   */
  @Index(IfNotZero.class)
  @NonFinal
  private long totalTagCount;

  @Index
  @IgnoreSave(IfNull.class)
  @NonFinal
  private List<Key<Tag>> groupKeys;

  public Key<Tag> getKey() {
    return Key.create(Tag.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public List<TagShard> getShards() {
    return Deref.deref(shardRefs);
  }

  public enum Type {
    NORMAL,
    GROUP
  }
}