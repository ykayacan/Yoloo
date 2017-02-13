package com.yoloo.android.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;

public final class HtmlUtil {

  public static Spanned fromHtml(Context context, @StringRes int stringRes) {
    return VersionUtil.hasN()
        ? Html.fromHtml(context.getString(stringRes), Html.FROM_HTML_MODE_COMPACT)
        : Html.fromHtml(context.getString(stringRes));
  }

  public static Spanned fromHtml(String text) {
    return VersionUtil.hasN()
        ? Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        : Html.fromHtml(text);
  }
}
