package com.yoloo.backend.media;

import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.media.transformer.MediaTransformer;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@ApiTransformer(MediaTransformer.class)
public class Media {

  @Id private String id;
  @Parent @NonFinal private Key<Account> parent;
  @Wither @NonFinal private String mime;
  @Wither @NonFinal private String url;

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  public Key<Media> getKey() {
    return Key.create(parent, getClass(), id);
  }
}