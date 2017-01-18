package com.yoloo.backend.media.size;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonRootName;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Media;

@JsonRootName("thumb")
@ApiResourceProperty(name = "thumb")
public class ThumbSize extends Media.Size {

  public ThumbSize(String url) {
    super(url);
  }

  @Override
  public String getUrl() {
    return createUrl(url, MediaConfig.THUMB_SIZE, true);
  }

  @Override
  public int getWidth() {
    return MediaConfig.THUMB_SIZE;
  }

  @Override
  public int getHeight() {
    return MediaConfig.THUMB_SIZE;
  }
}