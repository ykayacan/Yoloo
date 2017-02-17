package com.yoloo.android.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

public final class DisplayUtil {

  public static int dpToPx(int dp) {
    float density = Resources.getSystem().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  public static int getScreenWidth() {
    return Resources.getSystem().getDisplayMetrics().widthPixels;
  }

  public static boolean isInLandscapeMode(Context context) {
    boolean isLandscape = false;
    if (context.getResources().getConfiguration().orientation
        == Configuration.ORIENTATION_LANDSCAPE) {
      isLandscape = true;
    }
    return isLandscape;
  }
}