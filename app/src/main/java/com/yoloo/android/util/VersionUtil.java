package com.yoloo.android.util;

import android.os.Build;

public final class VersionUtil {

  public static boolean hasJellyBean() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }

  public static boolean hasL() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
  }

  public static boolean hasM() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  public static boolean hasN() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
  }
}
