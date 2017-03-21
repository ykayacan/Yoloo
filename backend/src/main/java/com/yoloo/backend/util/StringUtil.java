package com.yoloo.backend.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {

  public static ImmutableList<String> splitToList(@Nonnull String args, @Nonnull String delimiter) {
    return ImmutableList.copyOf(
        Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args));
  }

  public static ImmutableSet<String> split(@Nonnull String args, @Nonnull String delimiter) {
    return ImmutableSet.copyOf(Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args));
  }

  public static Iterable<String> splitToIterable(@Nonnull String args, @Nonnull String delimiter) {
    return Splitter.on(delimiter).trimResults().omitEmptyStrings().split(args);
  }
}
