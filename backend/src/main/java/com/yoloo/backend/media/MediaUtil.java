package com.yoloo.backend.media;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MediaUtil {

  public static String extractExtension(String mime) {
    return mime.substring(mime.indexOf("/") + 1);
  }
}
