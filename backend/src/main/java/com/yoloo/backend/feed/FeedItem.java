package com.yoloo.backend.feed;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.yoloo.backend.vote.Votable;
import java.util.List;

public interface FeedItem extends Votable {

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  <E> List<E> getShards();
}
