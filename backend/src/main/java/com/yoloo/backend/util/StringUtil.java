package com.yoloo.backend.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

  public static ImmutableList<String> splitToList(final String args, final String delimiter) {
    return ImmutableList.copyOf(
        Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args));
  }

  public static ImmutableSet<String> split(String args, String delimiter) {
    return ImmutableSet.copyOf(Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args));
  }
}
