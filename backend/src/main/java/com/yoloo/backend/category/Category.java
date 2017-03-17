package com.yoloo.backend.category;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.yoloo.backend.category.transformer.CategoryTransformer;
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
@ApiTransformer(CategoryTransformer.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Category {

  public static final String FIELD_TYPE = "type";
  public static final String FIELD_RANK = "rank";

  /**
   * cat:name
   */
  @Id
  private String id;

  @Wither
  private long posts;

  @Load(ShardGroup.class)
  @NonFinal
  private List<Ref<CategoryShard>> shardRefs;

  @Wither
  @NonFinal
  private String name;

  @Wither
  @NonFinal
  private Link imageUrl;

  /*@Index(IfNotZero.class)*/
  @Index
  @Wither
  @NonFinal
  private double rank;

  public static String extractNameFromKey(Key<Category> categoryKey) {
    return categoryKey.getName().substring(4); // Extract identifier
  }

  public static Key<Category> createKey(String categoryName) {
    return Key.create(Category.class, "cat:" + categoryName);
  }

  public Key<Category> getKey() {
    return Key.create(Category.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public List<CategoryShard> getShards() {
    return Deref.deref(shardRefs);
  }

  public static class ShardGroup {
  }
}
