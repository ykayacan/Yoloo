package com.yoloo.backend.media;

import com.google.common.net.MediaType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MimeUtil {

    public static boolean isValidMime(String mimeType) {
        MediaType mime = MediaType.parse(mimeType);
        return mime.is(MediaType.ANY_IMAGE_TYPE) || mime.is(MediaType.ANY_VIDEO_TYPE);
    }

    public static boolean isVideo(String mimeType) {
        MediaType mime = MediaType.parse(mimeType);
        return mime.is(MediaType.ANY_VIDEO_TYPE);
    }

    public static boolean isPhoto(String mimeType) {
        MediaType mime = MediaType.parse(mimeType);
        return mime.is(MediaType.ANY_IMAGE_TYPE);
    }
}
