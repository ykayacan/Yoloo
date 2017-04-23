package com.yoloo.backend.tag;

import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.yoloo.backend.tag.transformer.TagTransformer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
@ApiTransformer(TagTransformer.class)
public class Tag {

  public static final String FIELD_NAME = "name";
  public static final String FIELD_POST_COUNT = "postCount";
  public static final String FIELD_RANK = "rank";

  // tag:name
  @Id private String id;

  //@Load @NonFinal private List<Ref<TagShard>> shardRefs;

  @Index private String name;

  private long postCount;

  private double numOfDays;

  private double sumOfUsage;

  private double sumOfUsageSquared;

  @Index private double rank;

  public static String extractNameFromKey(Key<Tag> tagKey) {
    return tagKey.getName().split(":")[1]; // Extract identifier
  }

  public static Key<Tag> createKey(String tagName) {
    return Key.create(Tag.class, "tag:" + tagName);
  }

  public Key<Tag> getKey() {
    return Key.create(Tag.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  /*public List<TagShard> getShards() {
    return Deref.deref(shardRefs);
  }*/
}