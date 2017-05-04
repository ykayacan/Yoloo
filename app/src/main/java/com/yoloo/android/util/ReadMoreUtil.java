package com.yoloo.android.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntRange;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import com.yoloo.android.R;

public final class ReadMoreUtil {

  // Color is same for all read more texts, so we should cache it.
  private static final ForegroundColorSpan FOREGROUND_COLOR_SPAN =
      new ForegroundColorSpan(Color.parseColor("#9E9E9E"));

  private ReadMoreUtil() {
    // empty constructor
  }

  public static Spannable addReadMore(Context context, String text, @IntRange(from = 1) int count) {
    if (text.length() >= count) {
      String textWithReadMore =
          context.getString(R.string.label_feed_read_moreal, text.substring(0, count));
      Spannable span = Spannable.Factory.getInstance().newSpannable(textWithReadMore);
      span.setSpan(FOREGROUND_COLOR_SPAN, count + 2 /*omit three dot and whitespace */,
          textWithReadMore.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

      return span;
    }

    return Spannable.Factory.getInstance().newSpannable(text);
  }
}
