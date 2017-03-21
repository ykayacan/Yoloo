package com.yoloo.android.util;

import javax.annotation.Nullable;

public final class StringUtil {

  public static boolean isNullOrEmpty(@Nullable String string) {
    return string == null || string.length() == 0;
  }
}
