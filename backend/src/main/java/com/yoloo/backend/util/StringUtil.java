package com.yoloo.backend.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

public class StringUtil {

  public static List<String> split(@Nonnull String args, @Nonnull String delimiter) {
    return ImmutableList.copyOf(
        Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args));
  }

  public static Iterable<String> splitToIterable(@Nonnull String args, @Nonnull String delimiter) {
    return Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args);
  }
}
