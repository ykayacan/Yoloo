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
import org.joda.time.DateTime;

@Entity
@Cache
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = false)
@ApiTransformer(MediaTransformer.class)
public class MediaEntity {

  public static final String FIELD_CREATED = "created";

  @Id private Long id;

  @Parent private Key<Account> parent;

  @Wither private String mime;

  @Wither private String url;

  @JsonIgnore private String originalPath;

  @Index(IfNotChatMediaSource.class) private MediaOrigin mediaOrigin;

  @Index private DateTime created;

  public String getWebsafeId() {
    return getKey().toWebSafeString();
  }

  public String getWebsafeOwnerId() {
    return parent.toWebSafeString();
  }

  public Key<MediaEntity> getKey() {
    return Key.create(parent, getClass(), id);
  }

  public enum MediaOrigin {
    POST, PROFILE, CHAT;

    public static MediaOrigin parse(@Nonnull String mediaOrigin) {
      switch (mediaOrigin.toLowerCase()) {
        case "post":
          return POST;
        case "profile":
          return PROFILE;
        case "chat":
          return CHAT;
        default:
          throw new IllegalArgumentException("Given " + mediaOrigin + " is not a valid type!");
      }
    }
  }
}