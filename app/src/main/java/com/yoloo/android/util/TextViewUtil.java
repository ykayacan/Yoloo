package com.yoloo.android.util;

import android.content.Context;
import android.support.annotation.StyleRes;
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
}
