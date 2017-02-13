package com.yoloo.android.ui.widget.timeview;

import android.support.v4.util.ArrayMap;
import java.util.Date;

public class TimeManager {

  private static final ArrayMap<String, Integer> UNITS = new ArrayMap<>();

  private static TimeManager instance = null;

  static {
    UNITS.put("m", 60);
    UNITS.put("h", 3600);
    UNITS.put("d", 86400);
    UNITS.put("w", 604800);
    UNITS.put("mo", 2592000);
    UNITS.put("y", 31104000);
  }

  public static TimeManager getInstance() {
    if (instance == null) {
      instance = new TimeManager();
    }
    return instance;
  }

  private static long getCurrentTimeStamp() {
    return new Date().getTime() / 1000;
  }

  public String calculateTime(long timeStamp) {
    final long diff = getCurrentTimeStamp() - timeStamp;
     return diffToString(diff);
  }

  private String diffToString(long diff) {
    final long absDiff = Math.abs(diff);
    if (absDiff < UNITS.get("m")) {
      //NOW
      return diff < 0 ? TimeConstants.IN_FEW_SECONDS : TimeConstants.NOW;
    } else if (absDiff < UNITS.get("h")) {
      //MINS
      return getTimeString(diff, "m", TimeConstants.MINUTES);
    } else if (absDiff < UNITS.get("d")) {
      //HOURS
      return getTimeString(diff, "h", TimeConstants.HOURS);
    } else if (absDiff < UNITS.get("w")) {
      //DAYS
      return getTimeString(diff, "d", TimeConstants.DAYS);
    } else if (absDiff < UNITS.get("mo")) {
      //WEEKS
      return getTimeString(diff, "w", TimeConstants.WEEKS);
    } else if (absDiff < UNITS.get("y")) {
      //MONTHS
      return getTimeString(diff, "mo", TimeConstants.MONTHS);
    } else {
      //YEARS
      return getTimeString(diff, "y", TimeConstants.YEARS);
    }
  }

  private String getTimeString(long diff, String oneUnit, String manyThings) {
    final int unit = UNITS.get(oneUnit);

    if (diff < 0) {
      //FUTURE
      diff = Math.abs(diff);
      if (diff < 2 * unit) {
        return TimeConstants.IN_ONE_THING.get(oneUnit);
      } else {
        final int numberOfThings = Math.round((diff / unit));
        return String.valueOf(numberOfThings) + manyThings;
      }
    } else {
      //PAST
      if (diff < 2 * unit) {
        return TimeConstants.ONE_THING_AGO.get(oneUnit);
      } else {
        final int numberOfThings = Math.round((diff / unit));
        return String.valueOf(numberOfThings) + manyThings;
      }
    }
  }
}
