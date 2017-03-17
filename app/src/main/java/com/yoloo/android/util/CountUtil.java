package com.yoloo.android.util;

import android.support.v4.util.SparseArrayCompat;

public final class CountUtil {

  private static final SparseArrayCompat<String> SUFFIXES = new SparseArrayCompat<>(3);

  static {
    SUFFIXES.append(1_000, "k");
    SUFFIXES.append(1_000_000, "M");
    SUFFIXES.append(1_000_000_000, "B");
  }

  private CountUtil() {
    // empty constructor
  }

  public static String formatCount(long value) {
    if (value == Long.MIN_VALUE) {
      return formatCount(Long.MIN_VALUE + 1);
    } else if (value < 0) {
      return "-" + formatCount(-value);
    } else if (value < 1000) {
      return Long.toString(value); //deal with easy case
    }

    final int index = floorIndex(SUFFIXES, (int) value);

    final int divideBy = SUFFIXES.keyAt(index);
    final String suffix = SUFFIXES.valueAt(index);

    final long truncated = value / (divideBy / 10); // The number part getPost the output times 10
    final boolean hasDecimal = truncated < 1000
        && (truncated / 100d) != (truncated / 100)
        && (truncated * 10) % 10 != 0; // If the decimal part is equal to 0, return false.
    return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
  }

  private static int floorIndex(SparseArrayCompat<?> sparseArray, int key) {
    int index = sparseArray.indexOfKey(key);
    if (index < 0) {
      index = ~index - 1;
    }
    return index;
  }
}
