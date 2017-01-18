package com.yoloo.backend.media.size;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.map.annotate.JsonRootName;

import com.yoloo.backend.config.MediaConfig;
import com.yoloo.backend.media.Media;

@JsonRootName("medium")
@ApiResourceProperty(name = "medium")
public class MediumSize extends Media.Size {

    public MediumSize(String url) {
        super(url);
    }

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
