package com.yoloo.android.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import static android.content.res.Resources.getSystem;

public final class DisplayUtil {

  private DisplayUtil() {
  }

  public static int dpToPx(int dp) {
    float density = getSystem().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  public static int spToPx(int sp) {
    float scaledDensity = Resources.getSystem().getDisplayMetrics().scaledDensity;
    return Math.round(sp * scaledDensity);
  }

  public static int getScreenWidth() {
    return getSystem().getDisplayMetrics().widthPixels;
  }

  public static boolean isInLandscapeMode(Context context) {
    boolean isLandscape = false;
    if (context.getResources().getConfiguration().orientation
        == Configuration.ORIENTATION_LANDSCAPE) {
      isLandscape = true;
    }
    return isLandscape;
  }

  public static int getStatusBarHeight() {
    int statusBarHeight = 0;
    int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = Resources.getSystem().getDimensionPixelSize(resourceId);
    }

    return statusBarHeight;
  }
}
