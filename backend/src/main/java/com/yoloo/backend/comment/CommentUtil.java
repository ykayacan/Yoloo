package com.yoloo.backend.comment;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommentUtil {

  public static String trimContent(String text, int limit) {
    return text.length() > limit ? text.substring(0, limit) : text;
  }
}
