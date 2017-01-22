package com.yoloo.android.util;

import android.app.Activity;
import android.support.annotation.ColorInt;

public final class ViewUtil {

  public static void setStatusBarColor(Activity activity, @ColorInt int color) {
    if (VersionUtil.hasL()) {
      activity.getWindow().setStatusBarColor(color);
    }
  }
}
