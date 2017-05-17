package com.yoloo.backend.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MediaConfig {

  public static final String BASE_BUCKET = "yoloo-151719.appspot.com";

  public static final String GS_PREFIX = "/gs";

  public static final String SERVE_GROUP_WITH_ICON =
      GS_PREFIX + "/" + BASE_BUCKET + "/groups/with_icon";

  public static final String SERVE_GROUP_WITHOUT_ICON =
      GS_PREFIX + "/" + BASE_BUCKET + "/groups/without_icon";

  public static final String SERVE_TRAVELER_TYPES =
      GS_PREFIX + "/" + BASE_BUCKET + "/traveler-types";

  public static final String SERVE_FLAGS =
      GS_PREFIX + "/" + BASE_BUCKET + "/flags/icons";

  public static final String USER_MEDIAS_POSTS_PATH = "user-medias/posts";
  public static final String USER_MEDIAS_PROFILES_PATH = "user-medias/profiles";
  public static final String USER_MEDIAS_CHATS_PATH = "user-medias/chats";

  public static final int MINI_SIZE = 48;
  public static final int THUMB_SIZE = 150;
  public static final int LOW_SIZE = 340;
  public static final int MEDIUM_SIZE = 600;
  public static final int LARGE_SIZE = 800;
}