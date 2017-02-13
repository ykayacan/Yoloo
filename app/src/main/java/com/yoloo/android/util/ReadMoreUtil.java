package com.yoloo.android.util;

import android.text.Spanned;
import com.yoloo.android.data.model.firebase.ChatMessage;

public final class ReadMoreUtil {

  public static Spanned readMoreContent(String original, int end) {
    return HtmlUtil.fromHtml(original.length() >= end
        ? original.substring(0, end).concat("<font color=\"#9E9E9E\">... read more</font>")
        : original);
  }

  public static String readMoreContent(ChatMessage message, int end) {
    return message.getUsername()
        + ": "
        + ReadMoreUtil.readMoreContent(message.getMessage(), end);
  }
}
