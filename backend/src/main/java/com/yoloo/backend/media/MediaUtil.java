package com.yoloo.backend.media;

import com.google.common.net.MediaType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MediaUtil {

  @Nullable public static String createMediaFileName(@Nonnull String contentType) {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

    MediaType mediaType = MediaType.parse(contentType);

    String name;
    if (mediaType.is(MediaType.ANY_IMAGE_TYPE)) {
      name = "IMG_" + timeStamp + "." + mediaType.subtype();
    } else if (mediaType.is(MediaType.ANY_VIDEO_TYPE)) {
      name = "VID_" + timeStamp + "." + mediaType.subtype();
    } else {
      return null;
    }

    return name;
  }
}
