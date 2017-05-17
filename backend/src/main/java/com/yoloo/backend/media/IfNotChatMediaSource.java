package com.yoloo.backend.media;

import com.googlecode.objectify.condition.ValueIf;

class IfNotChatMediaSource extends ValueIf<MediaEntity.MediaOrigin> {
  @Override public boolean matchesValue(MediaEntity.MediaOrigin value) {
    return value != MediaEntity.MediaOrigin.CHAT;
  }
}
