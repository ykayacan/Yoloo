package com.yoloo.android.util;

import android.content.Context;
import java.util.Locale;

public final class LocaleUtil {

  private LocaleUtil() {
    // empty constructor
  }

  public static Locale getCurrentLocale(Context context) {
    if (VersionUtil.hasN()) {
      return context.getResources().getConfiguration().getLocales().get(0);
    } else {
      //noinspection deprecation
      return context.getResources().getConfiguration().locale;
    }
  }
}
