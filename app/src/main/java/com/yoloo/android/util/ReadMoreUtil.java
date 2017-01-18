package com.yoloo.android.util;

import android.text.Html;
import android.text.Spanned;

public final class ReadMoreUtil {

  public static Spanned readMoreContent(String original) {
    return Html.fromHtml(original.length() >= 200
        ? original.substring(0, 200).concat("<font color=\"#9E9E9E\">... read more</font>")
        : original);
  }
}
