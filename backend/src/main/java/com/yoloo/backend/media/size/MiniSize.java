package com.yoloo.backend.media.size;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Media;

@JsonRootName("mini")
@ApiResourceProperty(name = "mini")
public class MiniSize extends Media.Size {

  public MiniSize(String url) {
    super(url);
  }

  @Override
  public String getUrl() {
    return createUrl(url, MediaConfig.MINI_SIZE, true);
  }

  @Override
  public int getWidth() {
    return MediaConfig.MINI_SIZE;
  }

  @Override
  public int getHeight() {
    return MediaConfig.MINI_SIZE;
  }
}