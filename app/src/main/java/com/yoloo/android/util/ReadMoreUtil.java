package com.yoloo.android.util;

import android.text.Html;
import android.text.Spanned;

public final class ReadMoreUtil {

  public static Spanned readMoreContent(String original, int end) {
    return Html.fromHtml(original.length() >= end
        ? original.substring(0, end).concat("<font color=\"#9E9E9E\">... read more</font>")
        : original);
  }
}
