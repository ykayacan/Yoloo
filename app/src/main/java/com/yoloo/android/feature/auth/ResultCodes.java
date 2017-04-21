package com.yoloo.android.feature.auth;

import android.app.Activity;

/**
 * Result codes {@code startActivityForResult}.
 */
final class ResultCodes {
  /**
   * Sign in succeeded
   **/
  public static final int OK = Activity.RESULT_OK;

  /**
   * Sign in canceled by user
   **/
  public static final int CANCELED = Activity.RESULT_CANCELED;

  private ResultCodes() {
    // no instance
  }
}
