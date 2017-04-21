package com.yoloo.android.util;

import android.content.Context;
import android.support.annotation.StyleRes;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.widget.TextView;

public final class TextViewUtil {

  private TextViewUtil() {
    // empty constructor
  }

  public static void setTextAppearance(TextView textView, Context context, @StyleRes int styleRes) {
    if (VersionUtil.hasM()) {
      textView.setTextAppearance(styleRes);
    } else {
      textView.setTextAppearance(context, styleRes);
    }
  }

  // http://stackoverflow.com/questions/4096851/remove-underline-from-links-in-textview-android
  public static void stripUnderlines(Spannable s) {
    URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
    for (URLSpan span : spans) {
      int start = s.getSpanStart(span);
      int end = s.getSpanEnd(span);
      s.removeSpan(span);
      span = new URLSpanNoUnderline(span.getURL());
      s.setSpan(span, start, end, 0);
    }
  }

  private static class URLSpanNoUnderline extends URLSpan {
    URLSpanNoUnderline(String url) {
      super(url);
    }

    @Override public void updateDrawState(TextPaint ds) {
      super.updateDrawState(ds);
      ds.setUnderlineText(false);
    }
  }
}
