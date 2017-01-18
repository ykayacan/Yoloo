package com.yoloo.backend.vote;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import org.joda.time.DateTime;

public interface Votable {

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  <T> Key<T> getVotableKey();

  DateTime getCreated();
}
