package com.yoloo.backend.media.size;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonRootName;

import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Media;

@JsonRootName("low")
@ApiResourceProperty(name = "low")
public class LowSize extends Media.Size {

    public LowSize(String url) {
        super(url);
    }

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
