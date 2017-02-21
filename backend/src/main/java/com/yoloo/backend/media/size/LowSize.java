package com.yoloo.backend.media.size;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Size;
import lombok.AllArgsConstructor;

@JsonRootName("low")
@ApiResourceProperty(name = "low")
@AllArgsConstructor(staticName = "of")
public class LowSize extends Size {

  private String url;

  @Override
  public String getUrl() {
    return createUrl(url, MediaConfig.LOW_SIZE, false);
  }

  @Override
  public int getWidth() {
    return MediaConfig.LOW_SIZE;
  }

  @Override
  public int getHeight() {
    return MediaConfig.LOW_SIZE;
  }
}
