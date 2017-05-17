package com.yoloo.backend.algorithm;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * Reddit's hot sort
 * (popularized by reddit's news ranking)
 * http://amix.dk/blog/post/19588
 * Corrected for decay errors in post
 */
public class YolooHotRankAlgorithm {

  private static final int DEFAULT_DECAY = 45000;

  private static final DateTime BEGINNING = new DateTime(2017, 5, 12, 12, 0);

  private static final DecimalFormat FORMAT = new DecimalFormat("####.#######",
      DecimalFormatSymbols.getInstance(Locale.US));

  public static double calculate(long ups, long downs, DateTime date) {
    return calculate(ups, downs, date, DEFAULT_DECAY);
  }

  public static double calculate(long ups, long downs, DateTime time, int decay) {
    final long score = score(ups, downs);

    final double order = Math.log10(Math.max(Math.abs(score), 1));

    final int seconds = Seconds.secondsBetween(BEGINNING, time).getSeconds();

    return round(order + (double)seconds / decay);
  }

  private static double round(final double value) {
    return Double.parseDouble(FORMAT.format(value));
  }

  private static long score(long ups, long downs) {
    return ups - downs;
  }
}
