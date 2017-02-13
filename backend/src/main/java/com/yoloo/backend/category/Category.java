package com.yoloo.backend.category;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
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
public class Category {

  public static final String FIELD_NAME = "name";
  public static final String FIELD_TYPE = "type";
  public static final String FIELD_RANK = "rank";

  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private long id;

  @Load(ShardGroup.class)
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private List<Ref<CategoryShard>> shardRefs;

  private long questions;

  @Index
  @NonFinal
  private String name;

  @Index
  @NonFinal
  private Type type;

  @Index
  @NonFinal
  private double rank;

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public Key<Category> getKey() {
    return Key.create(Category.class, id);
  }

  @ApiResourceProperty(name = "id")
  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  public List<CategoryShard> getShards() {
    return Deref.deref(shardRefs);
  }

  public enum Type {
    CONTINENT,
    THEME
  }

  public static class ShardGroup {
  }
}
