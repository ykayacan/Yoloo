package com.yoloo.backend.media;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Parent;
import com.yoloo.backend.account.Account;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

@Entity
@Cache
@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Media {

  @Id
  private String id;

  @Parent
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Key<Account> parentAccountKey;

  private String mime;

  private String url;

  @Ignore
  private List<Size> sizes;

  @AllArgsConstructor
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static abstract class Size {

    @NonNull
    protected String url;

    protected static String createUrl(String url, int size, boolean crop) {
      Preconditions.checkArgument(url != null, "Url must not be null.");
      Preconditions.checkArgument(size != 0, "Invalid size: %s", size);

      url = url + "=s" + size;

      if (crop) {
        url = url + "-c";
      }

      return url;
    }

    public abstract String getUrl();

    @JsonProperty("w")
    public abstract int getWidth();

    @JsonProperty("h")
    public abstract int getHeight();
  }
}