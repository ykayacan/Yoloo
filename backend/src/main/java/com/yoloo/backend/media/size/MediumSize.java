package com.yoloo.backend.media.size;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Size;
import lombok.AllArgsConstructor;

@JsonRootName("medium")
@ApiResourceProperty(name = "medium")
@AllArgsConstructor(staticName = "of")
public class MediumSize extends Size {

  private String url;

  @Override
  public String getUrl() {
    return createUrl(url, MediaConfig.MEDIUM_SIZE, false);
  }

  @Override
  public int getWidth() {
    return MediaConfig.MEDIUM_SIZE;
  }

  @Override
  public int getHeight() {
    return MediaConfig.MEDIUM_SIZE;
  }
}
