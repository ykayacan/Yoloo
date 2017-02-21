package com.yoloo.backend.media.size;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Size;
import lombok.AllArgsConstructor;

@JsonRootName("thumb")
@ApiResourceProperty(name = "thumb")
@AllArgsConstructor(staticName = "of")
public class ThumbSize extends Size {

  private String url;

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