package com.yoloo.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;

public final class DisplayUtil {

  public static int dpToPx(int dp) {
    float density = Resources.getSystem().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  public static int getScreenWidth(Context context) {
    Point size = new Point();
    ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
    return size.x;
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