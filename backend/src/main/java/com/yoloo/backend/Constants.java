package com.yoloo.backend;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * API Keys, Client Ids and Audience Ids for accessing APIs and configuring
 * Cloud Endpoints.
 * When you deploy your solution, you need to use your own API Keys and IDs.
 * Please refer to the documentation for this sample for more details.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

  public static final String ADMIN_EMAIL = "admin@yolooapp.com";
  public static final String ADMIN_USERNAME = "yoloo_admin";

  /**
   * Firebase app url.
   */
  public static final String FIREBASE_APP_URL = "https://yoloo-151719.firebaseio.com";

  /**
   * Firebase secret path.
   */
  public static final String FIREBASE_SECRET_JSON_PATH =
      "/WEB-INF/yoloo-151719-firebase-crashreporting-469q9-dced69ab2a.json";

  /**
   * Google Cloud Messaging API key.
   */
  public static final String GCM_API_KEY = "AIzaSyA1ua3lGVu8vkdDwvxbX7sTvFP945tBG9s";

  /**
   * Android client ID from Google Cloud console.
   */
  public static final String ANDROID_CLIENT_ID =
      "175355244637-aeloianmbm6g0sh2ifc1t3dq6pc7caiq.apps.googleusercontent.com";

  /**
   * iOS client ID from Google Cloud console.
   */
  public static final String IOS_CLIENT_ID = "YOUR-IOS-CLIENT-ID";

  /**
   * Web client ID from Google Cloud console.
   */
  public static final String WEB_CLIENT_ID =
      "175355244637-cnod9pcii71rsqpjj17mp24v95uul63o.apps.googleusercontent.com";

  public static final String BASE64_CLIENT_ID =
      "NjM0MjE3NTM4Njc5LXNsZ3YwZ29nYjU0dnZlMTVrM3JpY2NjY2YxNmRzcDA2LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29t";

  /**
   * Audience ID used to limit access to some client to the API.
   */
  public static final String AUDIENCE_ID = WEB_CLIENT_ID;

  public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

  /**
   * API package name.
   */
  public static final String API_OWNER = "backend.yoloo.com";

  /**
   * API package path.
   */
  public static final String API_PACKAGE_PATH = "";

  public static final long TOKEN_EXPIRES_IN = 1814400; // 24 hours in seconds.
}
