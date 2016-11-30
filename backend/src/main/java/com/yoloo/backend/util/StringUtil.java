package com.yoloo.backend.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class StringUtil {

    public static ImmutableList<String> splitToList(final String args, final String rule) {
        return ImmutableList.copyOf(args.split("\\s*" + rule + "\\s*"));
    }

    public static ImmutableSet<String> splitToSet(final String args, final String rule) {
        return ImmutableSet.copyOf(args.split("\\s*" + rule + "\\s*"));
    }
}
