package com.yoloo.backend.media;

import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.common.base.Preconditions;

public abstract class Size {

  protected static String createUrl(String url, int size, boolean crop) {
    Preconditions.checkArgument(url != null, "Url must not be null.");
    Preconditions.checkArgument(size != 0, "Invalid size: %s", size);

    url = url + "=s" + size;

    if (crop) {
      url = url + "-c";
    }

    // force webp format
    url = url + "-rw";

    return url;
  }

  public abstract String getUrl();

  @JsonProperty("w")
  public abstract int getWidth();

  @JsonProperty("h")
  public abstract int getHeight();
}
