package com.yoloo.backend.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MediaConfig {

  public static final String BASE_URL = "yoloo-151719.appspot.com";
  public static final String MEDIA_BUCKET = BASE_URL + "/user-media";

  public static final String BUCKET_IMAGE_BASE_URL = "/gs/yoloo-151719.appspot.com";
  public static final String CATEGORY_BUCKET = BUCKET_IMAGE_BASE_URL + "/categories";

  public static final int MINI_SIZE = 48;
  public static final int THUMB_SIZE = 150;
  public static final int LOW_SIZE = 340;
  public static final int MEDIUM_SIZE = 600;
  public static final int LARGE_SIZE = 800;
}