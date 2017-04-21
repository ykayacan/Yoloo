package com.yoloo.backend.travelertype;

import com.google.api.server.spi.config.ApiTransformer;
import com.google.appengine.api.datastore.Link;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.yoloo.backend.group.TravelerGroupEntity;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.Wither;
import lombok.extern.java.Log;

@Log
@Cache
@Entity
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
@ApiTransformer(TravelerTypeTransformer.class)
public class TravelerTypeEntity {

  // traveler_type:name
  @Id private String id;

  @Wither private String name;

  @Wither private Link imageUrl;

  private List<Key<TravelerGroupEntity>> groupKeys;

  public static Key<TravelerTypeEntity> createKey(@Nonnull String name) {
    return Key.create(TravelerTypeEntity.class, "traveler_type:" + name);
  }

  public Key<TravelerTypeEntity> getKey() {
    return Key.create(TravelerTypeEntity.class, id);
  }

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }
}
