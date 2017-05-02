package com.yoloo.backend.group;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.yoloo.backend.group.transformer.TravelerGroupTransformer;
import com.yoloo.backend.util.Deref;
import java.util.List;
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
@ApiTransformer(TravelerGroupTransformer.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
public class TravelerGroupEntity {

  public static final String FIELD_TYPE = "type";
  public static final String FIELD_RANK = "rank";
  public static final String FIELD_NAME = "name";

  /**
   * group:name
   */
  @Id private String id;

  @Wither private long subscriberCount;

  @Wither private long postCount;

  @Load(ShardGroup.class) private List<Ref<TravelerGroupShard>> shardRefs;

  @Wither @Index private String name;

  @Wither private Link imageWithIconUrl;

  @Wither private Link imageWithoutIconUrl;

  @Index @Wither private double rank;

  @Wither @Ignore private boolean subscribed;

  public static String extractNameFromKey(Key<TravelerGroupEntity> groupKey) {
    return groupKey.getName().split(":")[1]; // Extract identifier
  }

  public static Key<TravelerGroupEntity> createKey(String groupName) {
    return Key.create(TravelerGroupEntity.class, "group:" + groupName);
  }

  public Key<TravelerGroupEntity> getKey() {
    return Key.create(TravelerGroupEntity.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public List<TravelerGroupShard> getShards() {
    return Deref.deref(shardRefs);
  }

  public static class ShardGroup {
  }
}
