package com.yoloo.backend.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.server.spi.config.ApiTransformer;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import com.yoloo.backend.media.transformer.MediaTransformer;
import javax.annotation.Nonnull;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
@ApiTransformer(MediaTransformer.class)
public class Media {

  @Id private String id;
  @Parent private Key<Account> parent;
  @Wither private String mime;
  @Wither private String url;
  @JsonIgnore private String originalPath;
  @Index private MediaOrigin mediaOrigin;

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  public Key<Media> getKey() {
    return Key.create(parent, getClass(), id);
  }

  public static MediaOrigin parse(@Nonnull String mediaOrigin) {
    return mediaOrigin.equals(MediaOrigin.POST.name()) ? MediaOrigin.POST : MediaOrigin.PROFILE;
  }

  public enum MediaOrigin {
    POST, PROFILE
  }
}