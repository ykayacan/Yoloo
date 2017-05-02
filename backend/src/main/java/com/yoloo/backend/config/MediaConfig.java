package com.yoloo.backend.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MediaConfig {

  public static final String BASE_URL = "yoloo-151719.appspot.com";

  public static final String STORAGE_PREFIX = "/gs/";

  public static final String BASE_BUCKET_URL = "yoloo-151719.appspot.com";

  public static final String SERVE_GROUP_BUCKET_WITH_ICON =
      STORAGE_PREFIX + BASE_BUCKET_URL + "/groups/with_icon";
  public static final String SERVE_GROUP_BUCKET_WITHOUT_ICON =
      STORAGE_PREFIX + BASE_BUCKET_URL + "/groups/without_icon";
  public static final String SERVE_TRAVELER_TYPES_BUCKET =
      STORAGE_PREFIX + BASE_BUCKET_URL + "/traveler-types";
  public static final String SERVE_USER_MEDIAS_BUCKET =
      STORAGE_PREFIX + BASE_BUCKET_URL + "/user-medias";
  public static final String SERVE_FLAGS_BUCKET =
      STORAGE_PREFIX + BASE_BUCKET_URL + "/flags/icons";

  public static final String USER_MEDIA_BUCKET = "user-medias";

  public static final int MINI_SIZE = 48;
  public static final int THUMB_SIZE = 150;
  public static final int LOW_SIZE = 340;
  public static final int MEDIUM_SIZE = 600;
  public static final int LARGE_SIZE = 800;
}